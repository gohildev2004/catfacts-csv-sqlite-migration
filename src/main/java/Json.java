import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

final class Json {

    static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()   // keep ' and = readable instead of '
            .create();

    private Json() {}
}
