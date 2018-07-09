package com.example.boris.bakingapp;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.boris.bakingapp.dagger.components.DaggerWidgetProviderComponent;
import com.example.boris.bakingapp.dagger.modules.WidgetProviderModule;
import com.example.boris.bakingapp.entity.Ingredient;
import com.example.boris.bakingapp.utils.StringUtils;
import com.example.boris.bakingapp.utils.WidgetDataHelper;

import java.util.List;

import javax.inject.Inject;

/**
 * Implementation of App Widget functionality.
 */
public class RecipesWidgetProvider extends AppWidgetProvider {

    @Inject
    WidgetDataHelper widgetDataHelper;

    public RecipesWidgetProvider() {
        super();
        DaggerWidgetProviderComponent.builder()
                .appComponent(RecipesApplication.component)
                .widgetProviderModule(new WidgetProviderModule())
                .build()
                .inject(this);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            String recipeName = widgetDataHelper.getRecipeNameFromPrefs(appWidgetId);

            widgetDataHelper
                    .getIngredientsList(widgetDataHelper.getChosenRecipePosition())
                    .take(1)
                    .subscribe(
                            // OnNext
                            ingredients ->
                                    RecipesWidgetProvider
                                            .updateAppWidgetContent(context, appWidgetManager, appWidgetId, recipeName,
                                                    ingredients),
                            // OnError
                            throwable ->
                                    Log.d("Error", "Error: unable to populate widget data."));
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);


        for (int appWidgetId : appWidgetIds) {
            widgetDataHelper.deleteRecipeFromPrefs(appWidgetId);
        }
    }

    public static void updateAppWidgetContent(Context context, AppWidgetManager appWidgetManager,
                                              int appWidgetId, String recipeName, List<Ingredient> ingredients) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list);
        views.setTextViewText(R.id.widget_recipe_name, recipeName);
        views.removeAllViews(R.id.widget_ingredients_container);

        for (Ingredient ingredient : ingredients) {
            RemoteViews ingredientView = new RemoteViews(context.getPackageName(),
                    R.layout.widget_item);

            String line = StringUtils.formatIngdedient(
                    context, ingredient.getIngredient(), ingredient.getQuantity(), ingredient.getMeasure());

            ingredientView.setTextViewText(R.id.widget_ingredient_name, line);
            views.addView(R.id.widget_ingredients_container, ingredientView);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
