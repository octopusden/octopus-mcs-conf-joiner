package org.octopusden.octopus.fileutils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


@SuppressWarnings({"JavaDoc"})
public class FileUtils {


    /**
     * Deletes the directory and all it's subdirectories.
     *
     * @param dir
     * @return <code>true</code> if and only if the directory is
     *         successfully deleted; <code>false</code> otherwise
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                if (!deleteDir(new File(dir, child))) {
                    return false;
                }
            }
        }

        return dir.delete();
    }


    public static void copy(String src, String dest) throws IOException {
        File source = new File(src);
        if (source.isDirectory()) {
            File destDirectory = new File(dest, source.getName());
            destDirectory.mkdirs();
            String[] children = source.list();
            for (String child : children) {
                copy(new File(source, child).getAbsolutePath(), destDirectory.getAbsolutePath());
            }
        } else {
            String destFileName = dest + File.separator + source.getName();
            copyFile(src, destFileName);
        }
    }

    public static void copyFile(String src, String dest) throws IOException {
        File source = new File(src);
        File destFile = new File(dest);
        if (!destFile.exists())
            destFile.createNewFile();
        FileInputStream in = new FileInputStream(source);
        FileOutputStream out = new FileOutputStream(destFile);
        int l;
        byte[] b = new byte[1024];
        while (true) {
            l = in.read(b);
            if (l == -1)
                break;
            out.write(b, 0, l);
        }
        in.close();
        out.close();
    }


  /**
   * Procedure separates filepath into Path and FileName.
   *
   * @param FilePath - Input File Path
   * @return true if succesful, false otherwise
   */
  public static boolean devideFilePathIntoParts( String FilePath,
                                                 StringBuffer DirName,
                                                 StringBuffer FileName )
  {
      @SuppressWarnings({ "HardcodedFileSeparator" })
      int iSepPos = Math.max( FilePath.lastIndexOf( '/' ), FilePath.lastIndexOf( '\\' ) );

      if ( iSepPos > 0 )
      {
          if ( DirName != null )
          {
              DirName.append( FilePath.substring( 0, iSepPos + 1 ) );
          }
          if ( FileName != null )
          {
              FileName.append( FilePath.substring( iSepPos + 1 ) );
          }
          return true;
      } else
      {
          return false;
      }
  }

  public static boolean isFileExist( String folderName )
  {
      File file = new File( folderName );
      return ( file.exists() && file.isFile() );
  }


  public static boolean isFolderExist( String folderName )
  {
      File file = new File( folderName );
      return ( file.exists() && file.isDirectory() );
  }

  public static boolean isFolderOrFileExist( String folderName )
  {
      File file = new File( folderName );
      return ( file.exists() );
  }


  public static String getFileName( String filePath )
  {
      if ( filePath == null ) return ( null );
      StringBuffer dirName = new StringBuffer( 255 );
      StringBuffer fileName = new StringBuffer( 255 );
      if ( devideFilePathIntoParts( filePath, dirName, fileName ) )
          return ( fileName.toString() );
      else
          return ( null );
  }

  public static String getFirstUnexistFolderWithNumericPrefix( String baseFolderName )
  {
      int i = 0;
      String folderName;
      do
      {
          //noinspection StringContatenationInLoop
          folderName = baseFolderName + "." + i;
          i++;
      } while ( isFolderOrFileExist( folderName ) );
      return ( folderName );
  }

  public static boolean makeRecursiveFolders( String folderName )
  {
      File folder = new File( folderName );
      return ( folder.exists() || folder.mkdirs() );
  }

  public static boolean makeRecursiveFoldersForFile( String fileName )
  {
      StringBuffer DirName = new StringBuffer();

      if ( !devideFilePathIntoParts( fileName, DirName, null ) )
          return ( false );

      return ( makeRecursiveFolders( DirName.toString() ) );
  }




}

