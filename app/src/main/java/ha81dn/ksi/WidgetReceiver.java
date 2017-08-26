package ha81dn.ksi;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class WidgetReceiver extends AppWidgetProvider {
    static AsyncTask<String, Void, String> currentTask;

    private static void prepareWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            applyOnClick(context, remoteViews, widgetId);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private static void applyOnClick(Context context, RemoteViews remoteViews, int widgetId) {
        remoteViews.setOnClickPendingIntent(R.id.ivImage, makePendingIntent(context, widgetId, "ITEM", "IMAGE"));
        remoteViews.setOnClickPendingIntent(R.id.tvMonTitle, makePendingIntent(context, widgetId, "ITEM", "MONDAY"));
        remoteViews.setOnClickPendingIntent(R.id.tvMonBody, makePendingIntent(context, widgetId, "ITEM", "MONDAY"));
        remoteViews.setOnClickPendingIntent(R.id.tvTueTitle, makePendingIntent(context, widgetId, "ITEM", "TUESDAY"));
        remoteViews.setOnClickPendingIntent(R.id.tvTueBody, makePendingIntent(context, widgetId, "ITEM", "TUESDAY"));
        remoteViews.setOnClickPendingIntent(R.id.tvWedTitle, makePendingIntent(context, widgetId, "ITEM", "WEDNESDAY"));
        remoteViews.setOnClickPendingIntent(R.id.tvWedBody, makePendingIntent(context, widgetId, "ITEM", "WEDNESDAY"));
        remoteViews.setOnClickPendingIntent(R.id.tvThuTitle, makePendingIntent(context, widgetId, "ITEM", "THURSDAY"));
        remoteViews.setOnClickPendingIntent(R.id.tvThuBody, makePendingIntent(context, widgetId, "ITEM", "THURSDAY"));
        remoteViews.setOnClickPendingIntent(R.id.tvFriTitle, makePendingIntent(context, widgetId, "ITEM", "FRIDAY"));
        remoteViews.setOnClickPendingIntent(R.id.tvFriBody, makePendingIntent(context, widgetId, "ITEM", "FRIDAY"));
    }

    private static PendingIntent makePendingIntent(Context context, int widgetId, String extraName, String extraValue) {
        Intent intent = new Intent(context, WidgetReceiver.class);
        intent.setAction("com.ha81dn.ksi.UPDATE");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.putExtra(extraName, extraValue);
        return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent makeIntent(Context context, int widgetId, String extraName, String extraValue) {
        Intent intent = new Intent(context, WidgetReceiver.class);
        intent.setAction("com.ha81dn.ksi.UPDATE");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.putExtra(extraName, extraValue);
        return intent;
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, WidgetReceiver.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        prepareWidget(context, appWidgetManager, allWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals("com.ha81dn.ksi.UPDATE")) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (widgetId == -1) return;

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            currentTask = new HttpAsyncTask();
            ((HttpAsyncTask) currentTask).context = context;
            ((HttpAsyncTask) currentTask).appWidgetManager = appWidgetManager;
            ((HttpAsyncTask) currentTask).targetWidgetId = widgetId;
            currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, intent.getStringExtra("ITEM"));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        prepareWidget(context, appWidgetManager, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            context.sendBroadcast(makeIntent(context, widgetId, "ITEM", "IMAGE"));
            context.sendBroadcast(makeIntent(context, widgetId, "ITEM", "MONDAY"));
            context.sendBroadcast(makeIntent(context, widgetId, "ITEM", "TUESDAY"));
            context.sendBroadcast(makeIntent(context, widgetId, "ITEM", "WEDNESDAY"));
            context.sendBroadcast(makeIntent(context, widgetId, "ITEM", "THURSDAY"));
            context.sendBroadcast(makeIntent(context, widgetId, "ITEM", "FRIDAY"));
        }
    }

    private static class HttpAsyncTask extends AsyncTask<String, Void, String> {
        AppWidgetManager appWidgetManager;
        Context context;
        int targetWidgetId;
        String item;
        Bitmap image;

        @SuppressWarnings("deprecation")
        @Override
        protected String doInBackground(String... urls) {
            URL myFileUrl;
            HttpsURLConnection conn;
            InputStream is;
            BufferedReader br;
            Calendar date;
            SimpleDateFormat sdf;
            StringBuilder sb;
            String result = "a=b";
            String line;
            String prefix = "";
            item = urls[0];
            int pos1, pos2;
            try {
                if (item.equals("")) return null;

                date = Calendar.getInstance(Locale.GERMAN);
                sdf = new SimpleDateFormat("MMMM", Locale.GERMAN);

                switch (item) {
                    case "IMAGE":
                        date = Calendar.getInstance(Locale.GERMAN);
                        date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        sdf = new SimpleDateFormat("MMMM", Locale.GERMAN);

                        myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                        conn = (HttpsURLConnection) myFileUrl.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        if (conn.getResponseCode() != 200) {
                            conn.disconnect();
                            date.add(Calendar.DATE, 1);
                            myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                            conn = (HttpsURLConnection) myFileUrl.openConnection();
                            conn.setDoInput(true);
                            conn.connect();
                            if (conn.getResponseCode() != 200) {
                                conn.disconnect();
                                date.add(Calendar.DATE, 1);
                                myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                                conn = (HttpsURLConnection) myFileUrl.openConnection();
                                conn.setDoInput(true);
                                conn.connect();
                                if (conn.getResponseCode() != 200) {
                                    conn.disconnect();
                                    date.add(Calendar.DATE, -9);
                                    myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                                    conn = (HttpsURLConnection) myFileUrl.openConnection();
                                    conn.setDoInput(true);
                                    conn.connect();
                                    if (conn.getResponseCode() != 200) {
                                        conn.disconnect();
                                        date.add(Calendar.DATE, 1);
                                        myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                                        conn = (HttpsURLConnection) myFileUrl.openConnection();
                                        conn.setDoInput(true);
                                        conn.connect();
                                        if (conn.getResponseCode() != 200) {
                                            conn.disconnect();
                                            date.add(Calendar.DATE, 1);
                                            myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                                            conn = (HttpsURLConnection) myFileUrl.openConnection();
                                            conn.setDoInput(true);
                                            conn.connect();
                                            if (conn.getResponseCode() != 200) {
                                                conn.disconnect();
                                                return "Kein Küchenschlacht-Foto innerhalb der letzten zwei Wochen gefunden!";
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is = conn.getInputStream();
                        br = new BufferedReader(new InputStreamReader(is));
                        sb = new StringBuilder();
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        conn.disconnect();
                        line = sb.toString();
                        pos1 = line.indexOf("twitter:image\" content=\"");
                        if (pos1 >= 0) {
                            pos1 += 24;
                            pos2 = line.indexOf("\"", pos1);
                            if (pos2 >= 0) {
                                myFileUrl = new URL(line.substring(pos1, pos2));
                                conn = (HttpsURLConnection) myFileUrl.openConnection();
                                conn.setDoInput(true);
                                conn.connect();
                                is = conn.getInputStream();
                                image = BitmapFactory.decodeStream(is);
                                conn.disconnect();
                                sdf = new SimpleDateFormat("'Foto vom ' dd. MMMM yyyy", Locale.GERMAN);
                                result = sdf.format(date.getTime());
                            }
                        }
                        return result;

                    case "MONDAY":
                        date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        prefix = "Montag";

                        break;
                    case "TUESDAY":
                        date.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                        prefix = "Dienstag";

                        break;
                    case "WEDNESDAY":
                        date.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                        prefix = "Mittwoch";

                        break;
                    case "THURSDAY":
                        date.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                        prefix = "Donnerstag";

                        break;
                    case "FRIDAY":
                        date.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                        prefix = "Freitag";

                        break;
                }

                myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                conn = (HttpsURLConnection) myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                if (conn.getResponseCode() != 200) {
                    conn.disconnect();
                    date.add(Calendar.DATE, 1);
                    myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                    conn = (HttpsURLConnection) myFileUrl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    if (conn.getResponseCode() != 200) {
                        conn.disconnect();
                        date.add(Calendar.DATE, 1);
                        myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                        conn = (HttpsURLConnection) myFileUrl.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        if (conn.getResponseCode() != 200) {
                            conn.disconnect();
                            date.add(Calendar.DATE, -9);
                            myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                            conn = (HttpsURLConnection) myFileUrl.openConnection();
                            conn.setDoInput(true);
                            conn.connect();
                            if (conn.getResponseCode() != 200) {
                                conn.disconnect();
                                date.add(Calendar.DATE, 1);
                                myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                                conn = (HttpsURLConnection) myFileUrl.openConnection();
                                conn.setDoInput(true);
                                conn.connect();
                                if (conn.getResponseCode() != 200) {
                                    conn.disconnect();
                                    date.add(Calendar.DATE, 1);
                                    myFileUrl = new URL("https://www.zdf.de/show/die-kuechenschlacht/die-kuechenschlacht-vom-" + date.get(Calendar.DAY_OF_MONTH) + "-" + sdf.format(date.getTime()).toLowerCase() + "-" + date.get(Calendar.YEAR) + "-100.html");
                                    conn = (HttpsURLConnection) myFileUrl.openConnection();
                                    conn.setDoInput(true);
                                    conn.connect();
                                    if (conn.getResponseCode() != 200) {
                                        conn.disconnect();
                                        return prefix + "Keine Küchenschlacht innerhalb der letzten zwei Wochen gefunden!= ";
                                    }
                                }
                            }
                        }
                    }
                }
                is = conn.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                conn.disconnect();
                line = sb.toString();
                pos1 = line.indexOf("twitter:title\" content=\"");
                if (pos1 >= 0) {
                    pos1 += 24;
                    pos2 = line.indexOf("\"", pos1);
                    if (pos2 >= 0) {
                        result = prefix + ": " + line.substring(pos1, pos2) + "=";
                        pos1 = line.indexOf("twitter:description\" content=\"");
                        if (pos1 >= 0) {
                            pos1 += 30;
                            pos2 = line.indexOf("\"", pos1);
                            if (pos2 >= 0) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    result += Html.fromHtml(line.substring(pos1, pos2), Html.FROM_HTML_MODE_LEGACY);
                                } else {
                                    result += Html.fromHtml(line.substring(pos1, pos2)).toString();
                                }
                            }
                        }
                    }
                }

            } catch (Exception error) {
                //return error.getLocalizedMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (isCancelled() || result == null) return;
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            applyOnClick(context, remoteViews, targetWidgetId);
            switch (item) {
                case "IMAGE":
                    if (image != null) remoteViews.setImageViewBitmap(R.id.ivImage, image);
                    remoteViews.setTextViewText(R.id.tvImageDate, result);
                    break;

                case "MONDAY":
                    remoteViews.setTextViewText(R.id.tvMonTitle, result.split("=")[0]);
                    remoteViews.setTextViewText(R.id.tvMonBody, result.split("=")[1]);
                    break;

                case "TUESDAY":
                    remoteViews.setTextViewText(R.id.tvTueTitle, result.split("=")[0]);
                    remoteViews.setTextViewText(R.id.tvTueBody, result.split("=")[1]);
                    break;

                case "WEDNESDAY":
                    remoteViews.setTextViewText(R.id.tvWedTitle, result.split("=")[0]);
                    remoteViews.setTextViewText(R.id.tvWedBody, result.split("=")[1]);
                    break;

                case "THURSDAY":
                    remoteViews.setTextViewText(R.id.tvThuTitle, result.split("=")[0]);
                    remoteViews.setTextViewText(R.id.tvThuBody, result.split("=")[1]);
                    break;

                case "FRIDAY":
                    remoteViews.setTextViewText(R.id.tvFriTitle, result.split("=")[0]);
                    remoteViews.setTextViewText(R.id.tvFriBody, result.split("=")[1]);
                    break;
            }
            //remoteViews.setTextViewText(R.id.update, context.getString(R.string.data_fetch, dots[idx]));
            /*
            remoteViews.setImageViewBitmap(R.id.update, result);
            SimpleDateFormat sdf;
            Calendar now;
            sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm 'Uhr'", Locale.getDefault());
            now = Calendar.getInstance();
            remoteViews.setTextViewText(R.id.timestamp, sdf.format(now.getTime()));
            */

            ComponentName thisWidget = new ComponentName(context, WidgetReceiver.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            for (int widgetId : allWidgetIds) {
                if (widgetId == targetWidgetId)
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
        }
    }
}
