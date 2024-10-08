package app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class Weather {

    private static final String ACCESS_KEY = "i will not show it :)";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите широту (lat): ");
        float lat = scanner.nextFloat();

        System.out.print("Введите долготу (lon): ");
        float lon = scanner.nextFloat();

        System.out.print("Введите количество дней прогноза (limit): ");
        int limit = scanner.nextInt();

        String url = "https://api.weather.yandex.ru/v2/forecast?lat=" + lat + "&lon=" + lon + "&limit=" + limit;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", ACCESS_KEY)
                .GET()
                .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {

                String responseBody = response.body();
                System.out.println("Полученные данные:\n" + responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);


                System.out.println("Текущая температура: " + getCurrentTemperature(jsonResponse) + "°C");

                if (getRealForecastsLength(jsonResponse) != limit) {
                    limit = getRealForecastsLength(jsonResponse);
                }

                System.out.println("Получен прогноз по " + limit + " дням");

                System.out.println("Средняя температура за " + limit + " дней: " + calculateAverageTemperature(jsonResponse) + "°C");
            } else {
                System.err.println("Ошибка при выполнении запроса. Код ответа: " + response.statusCode());
                System.err.println("Тело запроса: " + response.body());
            }
        }
        catch (Exception e) {
            System.err.println("Error occurred while sending GET request: " + e.getMessage());
        }
    }

    private static double getCurrentTemperature(JSONObject responseBodyJSON) {
        try {
            JSONObject fact = responseBodyJSON.getJSONObject("fact");
            return fact.getInt("temp");
        } catch (Exception e) {
            System.err.println("Ошибка при получении текущей температуры: " + e.getMessage());
            return 0;
        }
    }

    private static int getRealForecastsLength(JSONObject responseBodyJSON) {
        try {
            JSONArray forecasts = responseBodyJSON.getJSONArray("forecasts");
            return forecasts.length();
        } catch (Exception e) {
            System.err.println("Ошибка при получении реальной длины прогноза: " + e.getMessage());
            return 0;
        }
    }

    private static double calculateAverageTemperature(JSONObject responseBodyJSON) {
        try {
            JSONArray forecasts = responseBodyJSON.getJSONArray("forecasts");

            double totalTemp = 0;

            for (int i = 0; i < forecasts.length(); i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                JSONObject parts = forecast.getJSONObject("parts");
                JSONObject day = parts.getJSONObject("day");
                double dayTemp = day.getInt("temp_avg");

                totalTemp += dayTemp;
            }

            return totalTemp / getRealForecastsLength(responseBodyJSON);
        } catch (Exception e) {
            System.err.println("Ошибка при подсчете средней температуры: " + e.getMessage());
            return 0;
        }
    }
}
