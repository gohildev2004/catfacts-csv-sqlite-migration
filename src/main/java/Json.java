import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** Holds the one shared Gson instance the API uses to read and write JSON. */
final class Json {

    static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()   // keep ' and = readable instead of '
            .create();

    private Json() {}
}
