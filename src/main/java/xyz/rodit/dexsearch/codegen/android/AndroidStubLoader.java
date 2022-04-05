package xyz.rodit.dexsearch.codegen.android;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class AndroidStubLoader {

    public static ClassLoader loadStubs(File jar) throws MalformedURLException {
        return new URLClassLoader(new URL[]{jar.toURI().toURL()});
    }
}
