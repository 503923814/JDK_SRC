// AUTOMATICALLY GENERATED FILE - DO NOT EDIT
package sun.nio.fs;

class UnixConstants {
    private UnixConstants() {
    }

    static final int O_RDONLY = 0;
    static final int O_WRONLY = 1;
    static final int O_RDWR = 2;
    static final int O_APPEND = 0x400;
    static final int O_CREAT = 0x40;
    static final int O_EXCL = 0x80;
    static final int O_TRUNC = 0x200;
    static final int O_SYNC = 0x101000;
    static final int O_DSYNC = 0x1000;
    static final int O_NOFOLLOW = 0x20000;
    static final int S_IAMB = 0x1ff;
    static final int S_IRUSR = 256;
    static final int S_IWUSR = 128;
    static final int S_IXUSR = 64;
    static final int S_IRGRP = 32;
    static final int S_IWGRP = 16;
    static final int S_IXGRP = 8;
    static final int S_IROTH = 4;
    static final int S_IWOTH = 2;
    static final int S_IXOTH = 1;
    static final int S_IFMT = 0xf000;
    static final int S_IFREG = 0x8000;
    static final int S_IFDIR = 0x4000;
    static final int S_IFLNK = 0xa000;
    static final int S_IFCHR = 0x2000;
    static final int S_IFBLK = 0x6000;
    static final int S_IFIFO = 0x1000;
    static final int R_OK = 4;
    static final int W_OK = 2;
    static final int X_OK = 1;
    static final int F_OK = 0;
    static final int ENOENT = 2;
    static final int EACCES = 13;
    static final int EEXIST = 17;
    static final int ENOTDIR = 20;
    static final int EINVAL = 22;
    static final int EXDEV = 18;
    static final int EISDIR = 21;
    static final int ENOTEMPTY = 39;
    static final int ENOSPC = 28;
    static final int EAGAIN = 11;
    static final int ENOSYS = 38;
    static final int ELOOP = 40;
    static final int EROFS = 30;
    static final int ENODATA = 61;
    static final int ERANGE = 34;
    static final int EMFILE = 24;
    static final int AT_SYMLINK_NOFOLLOW = 0x100;
    static final int AT_REMOVEDIR = 0x200;
}
