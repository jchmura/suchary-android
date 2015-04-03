package pl.jakubchmura.suchary.android.joke.api.network;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.ParseException;

import pl.jakubchmura.suchary.android.joke.api.JSONParser;
import pl.jakubchmura.suchary.android.joke.api.model.APIJoke;
import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class JokeConverter implements Converter {
    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        Object object = null;
        InputStream in = null;
        try {
            in = body.in();
            String text = readText(in);
            JSONObject jsonObject = new JSONObject(text);

            if (type.equals(APIJoke.class)) {
                object = JSONParser.getAPIJoke(jsonObject);
            } else if (type.equals(APIResult.class)) {
                object = JSONParser.getAPIResult(jsonObject);
            } else if (type.equals(APIResult.APIJokes.class)) {
                object = JSONParser.getAPIJokes(jsonObject);
            }
        } catch (IOException | JSONException | ParseException e) {
            throw new ConversionException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
        return object;
    }

    @Override
    public TypedOutput toBody(Object object) {
        Object toSerialize;
        if (object instanceof APIResult.APIJokes) {
            APIResult.APIJokes apiJokes = (APIResult.APIJokes) object;
            toSerialize = new APIResult(((APIResult.APIJokes) object).size(), null, null, apiJokes);
        } else {
            toSerialize = object;
        }
        try {
            String encoding = "UTF-8";
            return new JsonTypedOutput(new Gson().toJson(toSerialize).getBytes(encoding), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    private String readText(InputStream in) throws IOException {
        return IOUtils.toString(in);
    }

    private static class JsonTypedOutput implements TypedOutput {
        private final byte[] jsonBytes;
        private final String mimeType;

        JsonTypedOutput(byte[] jsonBytes, String encode) {
            this.jsonBytes = jsonBytes;
            this.mimeType = "application/json; charset=" + encode;
        }

        @Override public String fileName() {
            return null;
        }

        @Override public String mimeType() {
            return mimeType;
        }

        @Override public long length() {
            return jsonBytes.length;
        }

        @Override public void writeTo(OutputStream out) throws IOException {
            out.write(jsonBytes);
        }
    }
}
