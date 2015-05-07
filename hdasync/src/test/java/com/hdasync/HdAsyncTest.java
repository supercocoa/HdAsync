package com.hdasync;

import android.util.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by scott on 15/4/25.
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class HdAsyncTest {

    @Mock
    HdAsync hdAsync;


    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void asyncTst() throws Exception {
        when(hdAsync.append(null)).thenReturn(null);
        verify(hdAsync).append(null);
        Log.d("HdAsyncTest", "testAsync");
    }

}
