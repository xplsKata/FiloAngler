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
            case "Illegal fishing threatens biodiversity in Leyte's Panaon Island":
                Utils.loadImage(imgHeader, R.drawable.news_illegal);
                break;
            case "iReel One IFC Smart Fishing Reel":
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

        contentMap.put("iReel One IFC Smart Fishing Reel",
                "<b>\"INTELLIGENTLY CHANGING THE WAY YOU FISH\"</b><br><br>" +
                        "Using <b>Bluetooth</b> to pair the iReel One with your smartphone or other Bluetooth devices and the <b>KastKing App</b>, iReel One employs highly accurate motion capture sensors in the spool assembly to provide anglers with precise casting metrics, including:<br>" +
                        "• Number of casts made<br>" +
                        "• Average distance<br>" +
                        "• Farthest cast<br>" +
                        "• Retrieve speed, and more<br><br>" +

                        "<b>ADVANCED DIGITAL BRAKING TECHNOLOGY</b><br><br>" +
                        "iReel One introduces <b>Intelligent Frequency Control (IFC)</b> to evaluate casting variables such as spool speed, inertia, and line tension to optimize performance in real-time, eliminating backlashes and ensuring smooth and accurate casts. The IFC microcontroller sensors within the frame and side plate analyze spool data and automatically apply or release a sophisticated <b>electromagnetic braking system</b> in a non-linear fashion.<br><br>" +

                        "<b>ENHANCED CASTABILITY</b><br><br>" +
                        "The uniquely shaped and rotating <b>Axis Eye</b> presents a very wide line-out aperture and a straight shot off the reel and through the line guides for longer and more accurate casts. The line guide features a <b>super slick silicon nitride coating</b> that offers excellent heat dissipation, allowing braid, monofilament, and fluorocarbon lines to glide freely across the surface with minimal restriction.<br><br>" +

                        "<b>BEAUTY COMBINED WITH COMFORT</b><br><br>" +
                        "Its sleek and stylish <b>electroplated finish</b> is both attractive and durable while maintaining a low-profile, <b>41.5mm design</b> for ultimate comfort. Ideal for those who palm their reel while twitching jerkbaits or prefer to keep a finger in contact with the line as a lure is falling in deep water.<br><br>" +

                        "<b>SPEED AND STRENGTH</b><br><br>" +
                        "The <b>7.2:1 gear ratio</b> is ideal for various fishing techniques, including:<br>" +
                        "• Topwater lures<br>" +
                        "• Jerkbaits<br>" +
                        "• Jigs, and more<br><br>" +
                        "The high gear ratio is useful when anglers need to quickly retrieve lures away from docks, weeds, and other cover. The <b>carbon fiber drag washer</b>, <b>7075 aluminum main gear</b>, and <b>brass pinion gear</b> work together to provide up to <b>16 pounds of smooth, fish-stopping power</b>."
        );

        contentMap.put("Illegal fishing threatens biodiversity in Leyte's Panaon Island",
                "<b>By Sarwell Meniano</b><br><br>" +
                        "Illegal fishing remains a major issue in <b>Southern Leyte's Panaon Island</b>, which is being proposed for inclusion in the <b>Expanded National Integrated Protected Areas System (ENIPAS) Act</b>. Consultations conducted by <b>Oceana</b> highlighted critical threats to marine biodiversity:<br>" +
                        "• Compressor fishing<br>" +
                        "• Night spearfishing<br><br>" +
                        "These activities cause significant environmental damage by:<br>" +
                        "• Harming coral reefs<br>" +
                        "• Disrupting marine ecosystems<br>" +
                        "• Threatening local livelihoods<br><br>" +
                        "Legislative action is urged to declare Panaon Island as a protected area to ensure sustainable management and conservation.<br><br>" +
                        "Panaon Island's coral reefs are <b>globally significant</b>, hosting endangered species including:<br>" +
                        "• Whale sharks<br>" +
                        "• Sea turtles<br><br>" +
                        "Despite progress, challenges like typhoon damage and overfishing amplify the urgency for protective measures.<br><br>" +
                        "The island encompasses the municipalities of:<br>" +
                        "• Liloan<br>" +
                        "• San Francisco<br>" +
                        "• Pintuyan<br>" +
                        "• San Ricardo<br><br>" +
                        "The region depends heavily on marine biodiversity for economic survival. (PNA)"
        );

    }

    private static String getContentByTitle(String title){
        return contentMap.getOrDefault(title, "No content found for the given title.");
    }
}