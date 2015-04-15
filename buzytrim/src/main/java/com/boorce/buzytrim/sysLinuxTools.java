package com.boorce.buzytrim;

import java.io.File;
import eu.chainfire.libsuperuser.Shell;

public class sysLinuxTools {

    public String getBusyPath() {
        if (new File("/system/bin/busybox").isFile()) {
            return "/system/bin/";
        } else if (new File("/system/sbin/busybox").isFile()) {
            return "/system/sbin/";
        } else if (new File("/system/xbin/busybox").isFile()) {
            return "/system/xbin/";
        } else {
            return null;
        }
    }

    public String getKernelRelease() {
        return Shell.SH.run("uname -r").toString();
    }

    

}
