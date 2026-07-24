package crypticlib.dependency;

import crypticlib.util.IOHelper;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Objects;

/**
 * Maven 仓库模型
 */
public class Repository extends AbstractXmlParser {

    private final String url;

    public Repository(@NotNull String url) {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public Repository(@NotNull Element node) throws ParseException {
        this(find("url", node, null));
    }

    public Repository() {
        this("https://maven.aliyun.com/repository/central");
    }

    /**
     * 从仓库下载依赖文件及其 SHA1 文件
     */
    public void downloadFile(@NotNull Dependency dep, @NotNull File out) throws IOException {
        String ext = out.getName().substring(out.getName().lastIndexOf('.') + 1);
        URL url = dep.getURL(this, ext);
        try {
            IOHelper.downloadFile(url, out);
            IOHelper.downloadFile(dep.getURL(this, ext + ".sha1"), new File(out.getPath() + ".sha1"));
        } catch (IOException e) {
            // 下载失败时清理可能的不完整文件
            if (out.exists()) {
                out.delete();
            }
            File sha1File = new File(out.getPath() + ".sha1");
            if (sha1File.exists()) {
                sha1File.delete();
            }
            throw e;
        }
    }

    /**
     * 获取依赖的最新版本
     */
    public void getLatestVersion(@NotNull Dependency dep) throws IOException {
        URL url = new URL(String.format("%s/%s/%s/maven-metadata.xml",
            getUrl(), dep.getGroupId().replace('.', '/'), dep.getArtifactId()));
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream ins = url.openStream();
            Document doc = builder.parse(ins);
            dep.setVersion(find("release", doc.getDocumentElement(), find("version", doc.getDocumentElement(), null)));
        } catch (IOException | RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @NotNull
    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Repository)) return false;
        Repository that = (Repository) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    @NotNull
    public String toString() {
        return "Repository{url='" + url + "'}";
    }
}
