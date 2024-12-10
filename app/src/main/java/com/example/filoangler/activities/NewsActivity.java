package com.example.filoangler.activities;

import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.filoangler.R;
import com.example.filoangler.Utils;

import java.util.HashMap;
import java.util.Map;

public class NewsActivity extends AppCompatActivity {

    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        title = getIntent().getStringExtra("title");

        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtDescription = findViewById(R.id.txtDescription);
        ImageView imgHeader = findViewById(R.id.imgHeader);

        switch (title){
            case "International fishing tourney trains spotlight on Siargao Island":
                Utils.loadImage(imgHeader, R.drawable.news_siargaogamefishing);
                break;
            case "Environment":
                Utils.loadImage(imgHeader, R.drawable.news_illegal);
                break;
            case "Gear":
                Utils.loadImage(imgHeader, R.drawable.news_ireel);
        }

        txtTitle.setText(title);
        txtDescription.setText(Html.fromHtml(getContentByTitle(title), Html.FROM_HTML_MODE_LEGACY));
    }

    private static final Map<String, String> contentMap = new HashMap<>();

    static {
        contentMap.put("International fishing tourney trains spotlight on Siargao Island",
                "<b>By Alexander Lopez</b><br><br>" +
                        "Thirty-seven professional anglers from <b>South Korea, Canada, Sweden, Hungary, the United States, and the Philippines</b> are battling it out in the 14th Siargao International Game Fishing Tournament in the Surigao del Norte island.<br><br>" +
                        "Booths with displays of agricultural products are also showcased within the event area, where local products are for sale.<br><br>" +
                        "Department of Tourism-Caraga (DOT-13) Director <b>Ivonnie Dumadag</b> recognized the pivotal role of the international market toward diversification and the development of industry in Siargao Island and the rest of the region.<br><br>" +
                        "\"The DOT supports the efforts of the local government unit (LGU) in sustaining the conduct of the international game fishing tournament despite the setbacks experienced in the past years,\" Dumadag said in a statement Friday.<br><br>" +
                        "DOT provided <b>PHP1 million</b> in financial assistance to the Pilar local government to help defray expenses.<br><br>" +
                        "\"The four-day tournament is expected to gather avid anglers and game fishing enthusiasts both local and from all over the world,\" Dumadag said.<br><br>" +
                        "The Police Regional Office in the Caraga Region (PRO-13) has assured the safety and security of anglers and visitors during the duration of the event, which kicked off Thursday, by providing <b>350 security forces</b> in Pilar and other strategic locations in Siargao Island.<br><br>" +
                        "Of the total number of security forces, <b>216 are from the Surigao del Norte Police Provincial Office</b> and <b>134 are from the Philippine Army</b>.<br><br>" +
                        "\"We maximize the deployment of personnel to safeguard domestic and foreign anglers, as well as the local and international tourists who will be witnessing the event and exploring the beautiful Siargao Islands,\" PRO-13 Director <b>Brig. Gen. Kirby John Kraft</b> said in a statement Friday. (PNA)"
        );
    }

    private static String getContentByTitle(String title){
        return contentMap.getOrDefault(title, "No content found for the given title.");
    }
}