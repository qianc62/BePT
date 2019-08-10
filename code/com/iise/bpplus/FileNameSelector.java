package com.iise.bpplus;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by little on 15-11-24.
 */
public class FileNameSelector implements FilenameFilter
{
    String extension = ".";

    public FileNameSelector(String fileExtensionNoDot)
    {
        extension += fileExtensionNoDot;
    }

    @Override
    public boolean accept(File dir, String name)
    {
        return name.endsWith(extension);
    }
}
