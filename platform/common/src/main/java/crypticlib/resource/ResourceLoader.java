package crypticlib.resource;

import crypticlib.CrypticLib;
import crypticlib.internal.PluginScanner;
import crypticlib.util.FunctionExecutor;
import crypticlib.util.IOHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceLoader {

    private static List<NetworkResource> loadResources() {
        List<NetworkResource> resources = new ArrayList<>();
        PluginScanner.INSTANCE.getAnnotatedClasses(NetworkResources.class).forEach(resourcesClass -> {
            NetworkResources networkResources = resourcesClass.getAnnotation(NetworkResources.class);
            resources.addAll(Arrays.asList(networkResources.resources()));
        });
        PluginScanner.INSTANCE.getAnnotatedClasses(NetworkResource.class).forEach(resourceClass -> {
            NetworkResource networkResource = resourceClass.getAnnotation(NetworkResource.class);
            resources.add(networkResource);
        });
        return resources;
    }

    public static void downloadResources(File folder) {
        IOHelper.info("Resources downloaded in " + FunctionExecutor.execute(() -> {
            IOHelper.info("Downloading resources...");
            loadResources().forEach(resource -> {
                File out = new File(folder, resource.filePath());
                //如果文件已经存在且NetworkResource.downloadIfExist为false,不再下载
                if (out.exists() && !resource.downloadIfExist()) {
                    return;
                }

                String[] downloadUrl = resource.downloadUrl();
                boolean success = false;
                for (String url : downloadUrl) {
                    try {
                        IOHelper.downloadFile(new URL(url), out);
                        success = true;
                        break;
                    } catch (IOException e) {
                        IOHelper.debug("[CrypticLib] Failed to download file from url " + url);
                        if (CrypticLib.debug()) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!success) {
                    if (resource.throwIfFailed()) {
                        throw new ResourceLoadException(resource.filePath());
                    } else {
                        IOHelper.info("&c[CrypticLib] Download resource failed: " + resource.filePath());
                    }
                }
            });
        }) + " ms.");
    }

}
