package m.t.musicplayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import m.t.musicplayer.fragment.MainFragment;



public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fresco.initialize(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        getSupportFragmentManager().beginTransaction().add(R.id.frame_main_fragmentContainer, new MainFragment(MainActivity.this)).commit();

    }


}
