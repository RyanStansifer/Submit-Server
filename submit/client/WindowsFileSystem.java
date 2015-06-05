package submit.client;

import java.io.File;
import java.util.Arrays;

import javax.swing.filechooser.FileSystemView;

      /*
	http://developer.java.sun.com/developer/bugParade/bugs/4458949.html

      java.io.File.listRoots() does a SecurityManager.checkRead() which
      causes the OS to try to access drive A: even when there is no disk,
      causing an annoying "abort, retry, ignore" popup message every time
      */



public class WindowsFileSystem {

   public static File getHomeDirectory() {
      final FileSystemView view = FileSystemView.getFileSystemView();
      return (view.getHomeDirectory());
   }

   public static File [] getMyComputerPathList() {
      final FileSystemView view = FileSystemView.getFileSystemView();
      File [] fileList = view.getRoots();

      /*
      if (fileList.length==1 && fileList[0].equals ("/")) return fileList;  // Unix
      */

      File desktop = null;
      for (int i = 0; i < fileList.length; i++) {
	 if (fileList[i].getName().endsWith("Desktop")) {
	    desktop = fileList[i];
	    break;
	 }
      }

      if (desktop == null) return null;

      //System.out.format ("found desktop: %s%n", desktop);
      fileList = desktop.listFiles();

      File myComputer = null;
      for (int i = 0; i < fileList.length; i++) {
	 //System.out.println (fileList[i]);
	 if (/*fileList[i].getName().startsWith("ShellFolder:") &&*/
	     FileSystemView.getFileSystemView().getSystemDisplayName(fileList[i]).equals("My Computer")) {
	    myComputer = fileList[i];
	    break;
	 }
      }

      if (myComputer == null) return null;
      //System.out.format ("found my computer: %s%n", myComputer);

      /*
        The files in ../My Computer/ are really all the available roots of
        the file system including remotely mounted file systems.
        File[] java.io.File.listRoots() are just the drive letters.  This
        list may contain drives (like A:) that cannot be used without getting
        the Windows OS involved.
      */

      fileList = myComputer.listFiles();
      final File [] pathList=new File [fileList.length];

      // This might do nothing ...
      for (int i = 0; i < fileList.length; i++) {
	 pathList[i] = new File(fileList[i].getPath());
         // System.out.format ("convert %s to %s%n", fileList[i], pathList[i]);
      }

      return pathList;
   }

   public static void print () {
      if (1==1) return;
      final FileSystemView view = FileSystemView.getFileSystemView();
      System.out.print ("Windows home dir:  ");
      System.out.println (view.getHomeDirectory().getAbsolutePath());

      /*
	On windows XP the roots are one :  ...\Desktop
       */

      /*
      final File [] roots = javax.swing.filechooser.FileSystemView.getFileSystemView().getRoots();
      System.out.print ("javax.swing.filechooser.FileSystemView.getFileSystemView().getRoots();");
      System.out.println ("  " + roots.length + " file(s).");

      for (int j=0;roots!=null && j<roots.length;j++) {
	 System.out.println ("Path =         " + roots[j].toString());
	 System.out.println ("Display name = " + view.getSystemDisplayName(roots[j]));
	 System.out.println ("Description  = " + view.getSystemTypeDescription(roots[j])); // null if no info
	 System.out.println ("Icon =         " + view.getSystemIcon(roots[j]));
	 System.out.print   (view.isTraversable(roots[j]) +", ");
	 System.out.println (roots[j].exists());   // ok for floppy?
      }
      */

      final File [] file_system_roots = File.listRoots();
      System.out.print ("java.io.File.listRoots();");
      System.out.println ("  " + file_system_roots.length + " file(s).");

      for (int j=0;file_system_roots!=null && j<file_system_roots.length;j++) {
	 System.out.println ("Path =         " + file_system_roots[j].toString());
	 System.out.println ("Is floppy drive? " + view.isFloppyDrive (file_system_roots[j]));
	 if (view.isFloppyDrive (file_system_roots[j])) continue;
	 System.out.println ("Display name = " + view.getSystemDisplayName(file_system_roots[j]));
	 System.out.println ("Description  = " + view.getSystemTypeDescription(file_system_roots[j])); // null if no info
	 System.out.println ("Icon =         " + view.getSystemIcon(file_system_roots[j]));
	 System.out.println (file_system_roots[j].exists());   // ok for floppy? no.
      }
    
   }

   public static void main (String[] args) {
      System.out.format ("system property 'os.name'   = %s%n", System.getProperty ("os.name"));
      System.out.format ("system property 'user.home' = %s%n", System.getProperty ("user.home"));
      final FileSystemView view = FileSystemView.getFileSystemView();

      // In XP "c:\..\Desktop" and "[c:\..\Desktop]"
      System.out.format ("file system view of home: %s%n", view.getHomeDirectory().getAbsolutePath());
      System.out.format ("file system roots:        %s%n", Arrays.asList(FileSystemView.getFileSystemView().getRoots()));

      System.out.format ("java.io.File.listRoots(): %s%n", Arrays.asList(File.listRoots()));


      final File [] roots = getMyComputerPathList();

      for (int j=0;roots!=null && j<roots.length;j++) {
         System.out.println ();
	 System.out.format ("Path =      %s%n", roots[j]);
	 System.out.println ("Path =         " + roots[j].getAbsolutePath());
	 System.out.println ("Display name = " + view.getSystemDisplayName(roots[j]));
	 System.out.println ("Description  = " + view.getSystemTypeDescription(roots[j])); // null if no info
	 System.out.format ("isFloppyDrive = %s%n", view.isFloppyDrive(roots[j]));
	 System.out.format ("System Icon   = %s%n",  view.getSystemIcon(roots[j]));
	 System.out.format ("isTraversable = %s%n", view.isTraversable(roots[j])); // always true
	 System.out.format ("File exists   = %s%n", roots[j].exists());   // ok for floppy
                            /*
	 final File f = new File (roots[j].getAbsolutePath());
	 System.out.println (f.exists());   // ok for floppy?
                            */
      }

   }

}

/*
  Path  =  A:\
  Display =
  Description = 3"-Inch Floppy Disk

  Path   =  C:\
  Display = Local Disk (C:)
  Description = Local Disk

  Path = U:\
  Display = ryan on 'atlantic server (Samba 2.2.8a) (udrive.fit.edu)' (U:)
  Description = Network Drive
 */
