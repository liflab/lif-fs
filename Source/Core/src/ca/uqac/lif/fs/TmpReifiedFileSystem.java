package ca.uqac.lif.fs;
public final class TmpReifiedFileSystem implements ReifiedFileSystem
{
  private final FileSystem m_backing;
  private final HardDisk m_local;
  private final java.nio.file.Path m_tmpRoot;

  private final Runnable m_beginBypass;
  private final Runnable m_endBypass;
  private final Runnable m_releaseLease;

  private boolean m_released = false;
  private boolean m_committed = false;

  public TmpReifiedFileSystem(FileSystem backing,
                              java.nio.file.Path tmpRoot,
                              Runnable beginBypass,
                              Runnable endBypass,
                              Runnable releaseLease)
      throws FileSystemException
  {
    m_backing = backing;
    m_tmpRoot = tmpRoot;
    m_beginBypass = beginBypass;
    m_endBypass = endBypass;
    m_releaseLease = releaseLease;

    m_local = new HardDisk(tmpRoot.toString()).open();
  }

  private void checkOpen() throws FileSystemException {
    if (m_released) throw new FileSystemException("Reified FS is released");
  }

  @Override
  public java.nio.file.Path toLocalPath(String path) throws FileSystemException
  {
    checkOpen();
    materialize(path);
    return m_tmpRoot.resolve(stripLeadingSlash(path)).normalize();
  }

  private void materialize(String path) throws FileSystemException
  {
    if (m_local.isFile(path) || m_local.isDirectory(path)) {
      return;
    }

    m_beginBypass.run();
    try
    {
      if (m_backing.isDirectory(path)) {
        mkdirs(m_local, path);
        return;
      }
      if (m_backing.isFile(path)) {
        mkdirs(m_local, parent(path));
        try (java.io.InputStream in = m_backing.readFrom(path);
             java.io.OutputStream out = m_local.writeTo(path)) {
          copy(in, out);
        } catch (java.io.IOException e) {
          throw new FileSystemException(e);
        }
        return;
      }
      // Not present remotely: ensure local parents so external tool can create it
      mkdirs(m_local, parent(path));
    }
    finally
    {
      m_endBypass.run();
    }
  }

  @Override
  public void commit() throws FileSystemException
  {
    checkOpen();
    m_beginBypass.run();
    try
    {
      // Mirror everything under tmpRoot back to backing
      try (java.util.stream.Stream<java.nio.file.Path> s = java.nio.file.Files.walk(m_tmpRoot))
      {
        s.forEach(p -> {
          if (p.equals(m_tmpRoot)) return;
          try {
            String rel = m_tmpRoot.relativize(p).toString()
                .replace(java.io.File.separatorChar, '/');
            String abs = "/" + rel;

            if (java.nio.file.Files.isDirectory(p)) {
              mkdirs(m_backing, abs);
            } else {
              mkdirs(m_backing, parent(abs));
              try (java.io.InputStream in = new java.io.BufferedInputStream(
                       new java.io.FileInputStream(p.toFile()));
                   java.io.OutputStream out = m_backing.writeTo(abs)) {
                copy(in, out);
              }
            }
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
      }
      catch (RuntimeException e)
      {
        Throwable c = e.getCause();
        if (c instanceof FileSystemException) throw (FileSystemException) c;
        throw new FileSystemException(e);
      }
      catch (java.io.IOException e)
      {
        throw new FileSystemException(e);
      }

      m_committed = true;
    }
    finally
    {
      m_endBypass.run();
    }
  }

  @Override
  public void release() throws FileSystemException
  {
    if (m_released) return;
    m_released = true;

    try {
      // recursive delete helper already exists in HardDisk :contentReference[oaicite:3]{index=3}
      HardDisk.deleteDirectoryRecursion(m_tmpRoot);
    } catch (java.io.IOException e) {
      throw new FileSystemException(e);
    } finally {
      try {
        m_releaseLease.run();
      } catch (RuntimeException e) {
        // unwrap if it wrapped a FileSystemException
        if (e.getCause() instanceof FileSystemException) throw (FileSystemException) e.getCause();
        throw e;
      }
    }
  }

  // ---- Delegate FileSystem methods to the local staging HardDisk ----

  @Override public FileSystem open() throws FileSystemException { checkOpen(); m_local.open(); return this; }

  @Override public java.util.List<String> ls() throws FileSystemException { checkOpen(); return m_local.ls(); }

  @Override public java.util.List<String> ls(String p) throws FileSystemException { checkOpen(); materialize(p); return m_local.ls(p); }

  @Override public boolean isDirectory(String p) throws FileSystemException { checkOpen(); materialize(p); return m_local.isDirectory(p); }

  @Override public boolean isFile(String p) throws FileSystemException { checkOpen(); materialize(p); return m_local.isFile(p); }

  @Override public long getSize(String p) throws FileSystemException { checkOpen(); materialize(p); return m_local.getSize(p); }

  @Override public java.io.OutputStream writeTo(String f) throws FileSystemException { checkOpen(); mkdirs(m_local, parent(f)); return m_local.writeTo(f); }

  @Override public java.io.InputStream readFrom(String f) throws FileSystemException { checkOpen(); materialize(f); return m_local.readFrom(f); }

  @Override public void chdir(String p) throws FileSystemException { checkOpen(); m_local.chdir(p); }

  @Override public void pushd(String p) throws FileSystemException { checkOpen(); m_local.pushd(p); }

  @Override public void popd() throws FileSystemException { checkOpen(); m_local.popd(); }

  @Override public void mkdir(String p) throws FileSystemException { checkOpen(); m_local.mkdir(p); }

  @Override public void rmdir(String p) throws FileSystemException { checkOpen(); m_local.rmdir(p); }

  @Override public void delete(String p) throws FileSystemException { checkOpen(); m_local.delete(p); }

  @Override public String pwd() throws FileSystemException { checkOpen(); return m_local.pwd(); }

  @Override public void close() throws FileSystemException { checkOpen(); m_local.close(); } // local staging close only

  // ---- Helpers ----

  private static void copy(java.io.InputStream in, java.io.OutputStream out) throws java.io.IOException {
    byte[] buf = new byte[8192];
    int r;
    while ((r = in.read(buf)) >= 0) out.write(buf, 0, r);
  }

  private static String stripLeadingSlash(String p) { return (p != null && p.startsWith("/")) ? p.substring(1) : p; }

  private static String parent(String p) {
    if (p == null) return "/";
    int i = p.lastIndexOf('/');
    return i <= 0 ? "/" : p.substring(0, i);
  }

  private static void mkdirs(FileSystem fs, String path) throws FileSystemException {
    if (path == null || path.isEmpty() || "/".equals(path)) return;
    String p = path.startsWith("/") ? path.substring(1) : path;
    String[] parts = p.split("/");
    String cur = "";
    for (String part : parts) {
      if (part.isEmpty()) continue;
      cur += "/" + part;
      try { fs.mkdir(cur); }
      catch (FileSystemException e) {
        // ignore if already exists; implementations differ
        if (!fs.isDirectory(cur)) throw e;
      }
    }
  }
}
