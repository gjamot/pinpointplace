package com.findmystuff.pinpointplace.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.findmystuff.pinpointplace.model.ApplicationModel;
import com.findmystuff.pinpointplace.model.Category;
import com.ppp.pinpointplace.R;

import java.util.List;

/**
 * Created by Greg on 15/01/2017.
 */

public class CategoryAdapter  extends ArrayAdapter<Category> {
    private ApplicationModel applicationModel;
    private Context context;
    private Category category;
    //stuffs est la liste des models à afficher
    public CategoryAdapter(Context context, List<Category> categories) {
        super(context, 0, categories);
        this.context = context;
        applicationModel = new ApplicationModel();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_category, parent, false);
        }

        CategoryAdapter.CategoryViewHolder viewHolder = (CategoryAdapter.CategoryViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new CategoryAdapter.CategoryViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.category_name);
            convertView.setTag(viewHolder);
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        category = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.name.setText(category.getName());

        ImageButton imgBtnUpdate = (ImageButton) convertView.findViewById(R.id.btn_update);
        imgBtnUpdate.setTag(position);
        imgBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer taggedPosition = (Integer) v.getTag();
                String categoryName = getItem(taggedPosition).getName();
                Intent intent = new Intent(context, UpdateCategoryActivity.class);
                intent.putExtra("CATEGORY_NAME", categoryName);
                context.startActivity(intent);
            }
        });

        ImageButton imgBtnDelete = (ImageButton) convertView.findViewById(R.id.btn_delete);

        // click listener for remove button
        imgBtnDelete.setTag(position);
        imgBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer taggedPosition = (Integer) v.getTag();
                final String categoryName = getItem(taggedPosition).getName();
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage("Etes-vous sûr(e) de vouloir supprimer la catégorie " + categoryName + " de la liste ainsi que tous ses objets associées?")
                        .setTitle("FindMyStuff");

                // Add the buttons
                builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (applicationModel != null) {
                            // Log.w("GJT", "Application model not null");
                           // Answers.getInstance().logContentView(new ContentViewEvent()
                            //       .putContentName("Deleting Category"));
                            //Answers.getInstance().logCustom(new CustomEvent("Deleting Category"));
                            //applicationModel.deleteCategoryFromName(context, categoryName);
                            applicationModel.deleteCategoryAndStuffAssociatedFromName(context, categoryName);
                        }
                        ((CategoryListActivity)context).onResume();
                        Toast.makeText((CategoryListActivity)context, "La catégorie a été bien été supprimée", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return convertView;
    }

    private class CategoryViewHolder {
        public TextView name;
    }
}
