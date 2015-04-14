/* Copyright (c) 2014 Mark Christopher Lauman
 * 
 * Licensed under the The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.                                                                  */
package ca.marklauman.dominionpicker;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;

/** Activity used to wrap the {@link FragmentMarket} when it
 *  is invoked from inside a supply.
 *  @author Mark Lauman                                   */
public class ActivityMarket extends ActionBarActivity {
	/** Key used to pass the supply pool to this activity (optional) */
	public static final String PARAM_SUPPLY = FragmentMarket.PARAM_SUPPLY;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        FrameLayout swapPanel = new FrameLayout(this);
        swapPanel.setId(R.id.content_frame);
        setContentView(swapPanel);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState != null) return;

        Bundle appExtras = getIntent().getExtras();
        FragmentMarket market = new FragmentMarket();
        Bundle args = new Bundle();
        args.putLongArray(FragmentMarket.PARAM_SUPPLY,
                          appExtras.getLongArray(PARAM_SUPPLY));
        market.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.content_frame, market)
                                   .commit();
	}
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // anything but the back button returns normal
        if(android.R.id.home != item.getItemId())
            return super.onOptionsItemSelected(item);
        // the back button closes the activity
        finish();
        return true;
	}
}