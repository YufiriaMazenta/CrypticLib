package crypticlib.internal.config.yaml;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentLoader {

    private static final Gson GSON = new Gson();

    public static @NotNull List<CommentLine> loadCommentLineList(String jsonArrayStr) {
        try {
            JsonArray jsonArray = GSON.fromJson(jsonArrayStr, JsonArray.class);
            List<CommentLine> commentLines = new ArrayList<>();
            jsonArray.forEach(comment -> {
                if (!(comment instanceof JsonNull))
                    commentLines.add(new CommentLine(null, null, comment.getAsString(), CommentType.BLOCK));
            });
            return commentLines;
        } catch (JsonSyntaxException e) {
            return Collections.singletonList(new CommentLine(null, null, jsonArrayStr, CommentType.BLOCK));
        }
    }

    public static @NotNull List<String> loadCommentList(String jsonArrayStr) {
        try {
            JsonArray jsonArray = GSON.fromJson(jsonArrayStr, JsonArray.class);
            List<String> commentLines = new ArrayList<>();
            jsonArray.forEach(comment -> {
                if (comment != null)
                    commentLines.add(comment.getAsString());
            });
            return commentLines;
        } catch (JsonSyntaxException e) {
            return Collections.singletonList(jsonArrayStr);
        }
    }

    public static String commentLineList2JsonArray(List<CommentLine> commentLines) {
        JsonArray array = new JsonArray();
        for (CommentLine commentLine : commentLines) {
            String comment = commentLine.getValue();
            if (comment != null)
                array.add(comment);
        }
        return GSON.toJson(array);
    }

    public static String commentList2JsonArray(List<String> comments) {
        JsonArray array = new JsonArray();
        for (String comment : comments) {
            if (comment != null)
                array.add(comment);
        }
        return GSON.toJson(array);
    }

}
