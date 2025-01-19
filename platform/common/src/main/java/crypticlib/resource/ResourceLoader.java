package crypticlib.resource;

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

    private static List<OnlineResource> loadResources() {
        List<OnlineResource> resources = new ArrayList<>();
        PluginScanner.INSTANCE.getAnnotatedClasses(OnlineResources.class).forEach(resourcesClass -> {
            OnlineResources onlineResources = resourcesClass.getAnnotation(OnlineResources.class);
            resources.addAll(Arrays.asList(onlineResources.resources()));
        });
        PluginScanner.INSTANCE.getAnnotatedClasses(OnlineResource.class).forEach(resourceClass -> {
            OnlineResource onlineResource = resourceClass.getAnnotation(OnlineResource.class);
            resources.add(onlineResource);
        });
        return resources;
    }

    public static void downloadResources(File folder) {
        IOHelper.info("All resources downloaded in " + FunctionExecutor.execute(() -> {
            loadResources().forEach(resource -> {
                String[] downloadUrl = resource.downloadUrl();
                boolean success = false;
                for (String url : downloadUrl) {
                    try {
                        IOHelper.downloadFile(new URL(url), new File(folder, resource.filePath()));
                        success = true;
                        break;
                    } catch (IOException ignored) {}
                }
                if (!success) {
                    if (resource.throwIfFailed()) {
                        throw new ResourceLoadException(resource.filePath());
                    } else {
                        IOHelper.info("&cDownload resource failed: " + resource.filePath());
                    }
                }
            });
        }) + " ms.");
    }

}
