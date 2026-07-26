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
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Objects;

/**
 * Maven 仓库模型
 */
public class Repository extends AbstractXmlParser {

    public static final Repository MAVEN_CENTRAL = new Repository("https://repo.maven.apache.org/maven2");
    public static final Repository MAVEN_CENTRAL_MIRROR_ALI = new Repository("https://maven.aliyun.com/repository/central");
    public static final Repository JITPACK = new Repository("https://jitpack.io");
    public static final Repository SONATYPE = new Repository("https://oss.sonatype.org/content/groups/public");

    private final String url;

    public Repository(@NotNull String url) {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public Repository(@NotNull Element node) throws ParseException {
        this(find("url", node, null));
    }

    /**
     * 从仓库下载依赖文件及其 SHA1 文件
     */
    public void downloadFile(@NotNull Dependency dep, @NotNull File out) throws IOException {
        String ext = out.getName().substring(out.getName().lastIndexOf('.') + 1);
        URL url = dep.getURL(this, ext);
        try {
            IOHelper.downloadFile(url, out);
        } catch (IOException e) {
            // 主文件下载失败时清理可能的不完整文件
            if (out.exists()) {
                out.delete();
            }
            throw e;
        }
        // .sha1 为可选的完整性校验文件, 下载失败时降级为跳过校验并保留主文件,
        // 不应连带删除已成功下载的主文件
        File sha1File = new File(out.getPath() + ".sha1");
        try {
            IOHelper.downloadFile(dep.getURL(this, ext + ".sha1"), sha1File);
        } catch (IOException e) {
            if (sha1File.exists()) {
                sha1File.delete();
            }
        }
    }

    /**
     * 获取依赖的最新版本
     */
    public void getLatestVersion(@NotNull Dependency dep) throws IOException {
        URL url = new URL(String.format("%s/%s/%s/maven-metadata.xml",
            url(), dep.groupId().replace('.', '/'), dep.artifactId()));
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 关闭 DOCTYPE 与外部实体解析, 防止 XXE / SSRF / billion-laughs
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "CrypticLib");
            try (InputStream ins = conn.getInputStream()) {
                Document doc = builder.parse(ins);
                dep.setVersion(find("release", doc.getDocumentElement(), find("version", doc.getDocumentElement(), null)));
            }
        } catch (IOException | RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @NotNull
    public String url() {
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
