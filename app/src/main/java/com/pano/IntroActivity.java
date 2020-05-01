package com.pano;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class IntroActivity extends AppCompatActivity {

    //Create placeholder for user's consent to record_audio permission.
    //This will be used in handling callback
    private final int MY_PERMISSIONS_RECORD_AUDIO = 99;
    FancyButton cont;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                // btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        setContentView(R.layout.intro_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!new PrefManager(getApplicationContext()).isFirstTimeLaunch()) {
            Intent in = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(in);
            finish();
        }

        setTitle("");
        cont = findViewById(R.id.cont);

        cont.setOnClickListener(v -> {

            permission();

        });


        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);

        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.welcome_slide,
                R.layout.welcome_slide0,
                R.layout.welcome_slide1,
                R.layout.welcome_slide2,
        };


        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(v ->
        {

        });

        btnNext.setOnClickListener(v -> {
            // checking for last page
            // if last page home screen will be launched
            int current = getItem(+1);
            if (current < layouts.length) {
                // move to next screen
                viewPager.setCurrentItem(current);
            } else {
                //launchHomeScreen();
            }

        });

        //addRoutes();
    }



    void addRoutes(){

        CollectionReference reference= FirebaseFirestore.getInstance().collection("routes");

        String routes="[{\"name\": \"Kabuga-mulindi-remera-sonatubes-rwandex-nyabugogo\", \"zone\": \"ZONE I\"}, {\"name\": \"Kabuga-mulindi-remera-sonatubes-rwandex-cbd\", \"zone\": \"ZONE I\"}, {\"name\": \"Kanombe-airport-remera-chez lando-kacyiru-nyabugogo\", \"zone\": \"ZONE I\"}, {\"name\": \"Kanombe-airport-remera-sonatubes-rwandex-cdb\", \"zone\": \"ZONE I\"}, {\"name\": \"Kanombe-airport-remera-chez lando-kimihurura-cbd\", \"zone\": \"ZONE I\"}, {\"name\": \"Kicukiro-sonatubes-remera-airport-kanombe\", \"zone\": \"ZONE I\"}, {\"name\": \"Masaka-masaka hospital-kabuga\", \"zone\": \"ZONE I\"}, {\"name\": \"Masaka-rusheshe\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-12-masoro(uaac)\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-nyarugunga-busanza\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-15-ndera-musave\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-chez lando-kacyiru-nyabugogo\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-rubilizi-busanza\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-mulindi-masaka\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-sonatubes-kicukiro centre-nyanza\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-sonatubes-rwandex-cbd\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-sonatubes-rwandex-gikondo-bwerankoli\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-12-sez\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-kanombe-kibaya\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-mulindi-kabuga\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-sonatubes-rwandex-nyabugogo\", \"zone\": \"ZONE I\"}, {\"name\": \"Rubilizi-remera-chez lando-kacyiru-nyabugogo\", \"zone\": \"ZONE I\"}, {\"name\": \"Rubilizi-remera-sonatubes-rwandex-cbd\", \"zone\": \"ZONE I\"}, {\"name\": \"Remera-mulindi-gasogi (cyaruzinge)\", \"zone\": \"ZONE I\"}, {\"name\": \"Bwerankoli-gikondo-segeem-rugunga-cbd\", \"zone\": \"ZONE II\"}, {\"name\": \"Bwerankoli-nyenyeli-segeem-kanogo-nyabugogo\", \"zone\": \"ZONE II\"}, {\"name\": \"Bwerankoli-segeem-rwandex-sonatubes-kimironko\", \"zone\": \"ZONE II\"}, {\"name\": \"Gahanga-mugendo-nyaruyenzi\", \"zone\": \"ZONE II\"}, {\"name\": \"Gikondo-rebero-nyarurama\", \"zone\": \"ZONE II\"}, {\"name\": \"Nyanza-kicukiro centre-zion temple-rwandex-nyabugogo\", \"zone\": \"ZONE II\"}, {\"name\": \"Nyanza-kicukiro centre-zion temple-magerwa-bwerankoli\", \"zone\": \"ZONE II\"}, {\"name\": \"Nyanza-kicukiro centre-kagarama-muyange\", \"zone\": \"ZONE II\"}, {\"name\": \"Nyanza-gahanga\", \"zone\": \"ZONE II\"}, {\"name\": \"Nyanza-karembure\", \"zone\": \"ZONE II\"}, {\"name\": \"Nyanza-kicukiro centre-sonatubes-gishushu-kacyiru\", \"zone\": \"ZONE II\"}, {\"name\": \"Nyanza-kicukiro centre-sonatubes-chez lando-kimironko\", \"zone\": \"ZONE II\"}, {\"name\": \"Nyanza-kicukiro centre-gatenga-magerwa-cbd\", \"zone\": \"ZONE II\"}, {\"name\": \"Nyanza-centre-gatenga-magerwa-nyabugogo\", \"zone\": \"ZONE II\"}, {\"name\": \"St joseph-centre de sante-sonatubes-rwandex-cbd\", \"zone\": \"ZONE II\"}, {\"name\": \"St joseph-cs-sonatubes-rwandex-nyabugogo\", \"zone\": \"ZONE II\"}, {\"name\": \"Bumbogo-gikomero-rutunga\", \"zone\": \"ZONE III\"}, {\"name\": \"Bumbogo-kimironko-reb (controle technique)-chez lando-kacyiru-nyabugogo\", \"zone\": \"ZONE III\"}, {\"name\": \"Bumbogo-kimironko-reb (controle technique)-chez lando-kimihurura-cbd\", \"zone\": \"ZONE III\"}, {\"name\": \"Bumbogo-zindiro-mushimire-kimironko\", \"zone\": \"ZONE III\"}, {\"name\": \"Cbd-kimihurura-kbc-kacyiru-mama sportif-gacuriro-kagugu\", \"zone\": \"ZONE III\"}, {\"name\": \"Cbd-kimihurura-rdb-nyarutarama-kinyinya\", \"zone\": \"ZONE III\"}, {\"name\": \"Cbd-kinamba-gakinjiro-fawe-kagugu-batsinda\", \"zone\": \"ZONE III\"}, {\"name\": \"Cbd-kinamba-ulk-fawe-kagugu\", \"zone\": \"ZONE III\"}, {\"name\": \"Kabuga-mulindi-12-kimironko-kibagabaga-utexrwa-kinamba-nyabugogo\", \"zone\": \"ZONE III\"}, {\"name\": \"Kimironko-12-mulindi-kabuga\", \"zone\": \"ZONE III\"}, {\"name\": \"Kimironko-12-mulindi-masaka\", \"zone\": \"ZONE III\"}, {\"name\": \"Kimironko-kibagabaga-kagugu-batsinda\", \"zone\": \"ZONE III\"}, {\"name\": \"Kimironko-kibagabaga-kinyinya\", \"zone\": \"ZONE III\"}, {\"name\": \"Kimironko-kibagabaga-utexrwa-kinamba-nyabugogo\", \"zone\": \"ZONE III\"}, {\"name\": \"Kimironko-kie-bibare-mushumba mwiza-remera\", \"zone\": \"ZONE III\"}, {\"name\": \"Kimironko-kie-stadium-chez lando-kacyiru-nyabugogo\", \"zone\": \"ZONE III\"}, {\"name\": \"Kimironko-kie-stadium-chez lando-kimihurura-cbd\", \"zone\": \"ZONE III\"}, {\"name\": \"Kimironko-zindiro-bumbogo-sez\", \"zone\": \"ZONE III\"}, {\"name\": \"Kimironko-zindiro-masizi\", \"zone\": \"ZONE III\"}, {\"name\": \"Kinyinya-birembo-kami\", \"zone\": \"ZONE III\"}, {\"name\": \"Nyabugogo-kinamba-gakinjiro-fawe-batsinda\", \"zone\": \"ZONE III\"}, {\"name\": \"Nyabugogo-kinamba-kagugu-batsinda-gasanze\", \"zone\": \"ZONE III\"}, {\"name\": \"Nyabugogo-kinamba-ulk-kagugu\", \"zone\": \"ZONE III\"}, {\"name\": \"Nyabugogo-kinamba-utexrwa-kinyinya\", \"zone\": \"ZONE III\"}, {\"name\": \"Remera-chez lando-nyarutarama-kagugu-batsinda\", \"zone\": \"ZONE III\"}, {\"name\": \"Cbd-down town-gakinjiro-gitega-biryogo-rafiki-ku rya nyuma\", \"zone\": \"ZONE IV\"}, {\"name\": \"Down town-nyabugogo\", \"zone\": \"ZONE IV\"}, {\"name\": \"Down town-nyabugogo-gatsata-karuruma-nyacyonga\", \"zone\": \"ZONE IV\"}, {\"name\": \"Down town-nyabugogo-kimisagara-nyakabanda-tapis rouge-ku rya nyuma\", \"zone\": \"ZONE IV\"}, {\"name\": \"Nyacyonga-nduba\", \"zone\": \"ZONE IV\"}, {\"name\": \"Nyabugogo-giticyinyoni-nzove(skol)-rutonde\", \"zone\": \"ZONE IV\"}, {\"name\": \"Nyabugogo-kamuhanda-ruyenzi\", \"zone\": \"ZONE IV\"}, {\"name\": \"Nyabugogo-karuruma-gihogwe-jali\", \"zone\": \"ZONE IV\"}, {\"name\": \"Nyabugogo-karuruma-nyacyonga\", \"zone\": \"ZONE IV\"}, {\"name\": \"Nyabugogo-ruyenzi-bishenyi\", \"zone\": \"ZONE IV\"}, {\"name\": \"Nyacyonga-rutunga\", \"zone\": \"ZONE IV\"}, {\"name\": \"Nyamirambo (rya nyuma)-40-biryogo-csk-kanogo-nyabugogo\", \"zone\": \"ZONE IV\"}, {\"name\": \"Nyamirambo-rwarutabura-mageragere\", \"zone\": \"ZONE IV\"}, {\"name\": \"Nyabugogo-ruyenzi-runda (gihara)\", \"zone\": \"ZONE IV\"}]";

        try{
            JSONArray array=new JSONArray(routes);
            Toasty.info(getApplicationContext(),"Decoding...").show();


            for(int a=0;a<array.length();a++){

                JSONObject object=array.getJSONObject(a);

                Map<String,String> route=new HashMap<>();
                route.put("name",object.getString("name"));
                route.put("zone",object.getString("zone"));

                reference.document().set(route);

            }
        }catch (Exception e){

            e.printStackTrace();
            Toasty.error(getApplicationContext(),"Error").show();

        }
    }


    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public void permission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Request Location Permission
            requestLocationPermissions();

        } else {

            new PrefManager(getApplicationContext()).setFirstTimeLaunch(false);
            Intent in = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(in);
            finish();

        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(IntroActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(IntroActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(IntroActivity.this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(IntroActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(IntroActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(IntroActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {


            Intent in = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(in);
            finish();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_righ);
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
