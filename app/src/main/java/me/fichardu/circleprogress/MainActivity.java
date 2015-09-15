package me.fichardu.circleprogress;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private CircleProgress mProgressView;
    private View mStartBtn;
    private View mStopBtn;
    private View mResetBtn;

    float factor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressView = (CircleProgress) findViewById(R.id.progress);
        mProgressView.startAnim();
        mStartBtn = findViewById(R.id.start_btn);
        mStartBtn.setOnClickListener(this);
        mStopBtn = findViewById(R.id.stop_btn);
        mStopBtn.setOnClickListener(this);
        mResetBtn = findViewById(R.id.reset_btn);
        mResetBtn.setOnClickListener(this);

        SeekBar mSeekBar = (SeekBar) findViewById(R.id.out_seek);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                factor  = progress / 100f;
                mProgressView.setRadius(factor);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == mStartBtn) {
            mProgressView.startAnim();
        } else if (v == mStopBtn) {
            mProgressView.stopAnim();
        } else if (v == mResetBtn) {
            mProgressView.reset();
        }
    }
}
