package com.iessf.nocuelguesapp.data;

import com.iessf.nocuelguesapp.domain.model.Category;
import com.iessf.nocuelguesapp.domain.model.ExpirationItem;
import com.iessf.nocuelguesapp.domain.model.Urgency;
import com.iessf.nocuelguesapp.domain.repository.ExpirationRepository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda los vencimientos en el dispositivo como un arreglo JSON dentro de SharedPreferences.
 */
public class ExpirationItemRepository implements ExpirationRepository {

    private static final String TAG = "ExpirationItemRepo";
    private static final String PREFERENCES_NAME = "expiration_items";
    private static final String KEY_ITEMS = "items";

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_EXPIRATION_DATE = "expirationDate";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_URGENCY = "urgency";
    private static final String FIELD_NOTE = "note";

    private final SharedPreferences preferences;

    public ExpirationItemRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /** Devuelve todos los ítems guardados; el orden de presentación lo define la capa de aplicación. */
    public List<ExpirationItem> getAll() {
        return readItems();
    }

    @Nullable
    public ExpirationItem findById(String id) {
        for (ExpirationItem item : readItems()) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /** Agrega el ítem o reemplaza al guardado con el mismo identificador. */
    public void save(ExpirationItem item) {
        List<ExpirationItem> items = readItems();
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            items.add(item);
        }
        writeItems(items);
    }

    public void delete(String id) {
        List<ExpirationItem> items = readItems();
        items.removeIf(item -> item.getId().equals(id));
        writeItems(items);
    }

    private List<ExpirationItem> readItems() {
        List<ExpirationItem> items = new ArrayList<>();
        String json = preferences.getString(KEY_ITEMS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                items.add(fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException | IllegalArgumentException | DateTimeParseException e) {
            Log.e(TAG, "Stored items could not be read", e);
        }
        return items;
    }

    private void writeItems(List<ExpirationItem> items) {
        JSONArray array = new JSONArray();
        try {
            for (ExpirationItem item : items) {
                array.put(toJson(item));
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Items could not be converted to JSON", e);
        }
        preferences.edit().putString(KEY_ITEMS, array.toString()).apply();
    }

    private static JSONObject toJson(ExpirationItem item) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(FIELD_ID, item.getId());
        object.put(FIELD_NAME, item.getName());
        object.put(FIELD_EXPIRATION_DATE, item.getExpirationDate().toString());
        object.put(FIELD_CATEGORY, item.getCategory().name());
        object.put(FIELD_URGENCY, item.getUrgency().name());
        object.put(FIELD_NOTE, item.getNote());
        return object;
    }

    private static ExpirationItem fromJson(JSONObject object) throws JSONException {
        return new ExpirationItem(
                object.getString(FIELD_ID),
                object.getString(FIELD_NAME),
                LocalDate.parse(object.getString(FIELD_EXPIRATION_DATE)),
                Category.valueOf(object.getString(FIELD_CATEGORY)),
                Urgency.valueOf(object.getString(FIELD_URGENCY)),
                object.optString(FIELD_NOTE, ""));
    }
}
