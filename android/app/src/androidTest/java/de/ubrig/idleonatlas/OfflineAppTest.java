package de.ubrig.idleonatlas;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import android.content.pm.PackageManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class OfflineAppTest {
    private String js(ActivityScenario<MainActivity> scenario,String script) throws Exception {
        AtomicReference<String> result=new AtomicReference<>();CountDownLatch latch=new CountDownLatch(1);
        scenario.onActivity(activity->activity.webView.evaluateJavascript(script,value->{result.set(value);latch.countDown();}));
        assertTrue("JavaScript did not return",latch.await(5,TimeUnit.SECONDS));return result.get();
    }
    private void waitFor(ActivityScenario<MainActivity> scenario,String condition) throws Exception {
        long deadline=System.currentTimeMillis()+30000;
        while(System.currentTimeMillis()<deadline){if("true".equals(js(scenario,condition)))return;Thread.sleep(150);}
        fail("Condition failed: "+condition+"; document="+js(scenario,"document.body.innerText.slice(-800)"));
    }
    @Test public void offlineStartupSearchAndNativeBridge() throws Exception {
        assertEquals(PackageManager.PERMISSION_DENIED,InstrumentationRegistry.getInstrumentation().getTargetContext().checkSelfPermission("android.permission.INTERNET"));
        try(ActivityScenario<MainActivity> s=ActivityScenario.launch(MainActivity.class)){
            waitFor(s,"document.querySelector('main h1')?.textContent.includes('Dein nächster Schritt')===true");
            assertEquals("true",js(s,"typeof window.nativeBackup?.postMessage==='function'"));
            js(s,"location.hash='database?q=Copper%20Ore'");
            waitFor(s,"!!document.querySelector('.entity[href=\"#entry/item%3ACopper\"]')");
            js(s,"document.querySelector('.entity[href=\"#entry/item%3ACopper\"]').click()");
            waitFor(s,"document.querySelector('main h1')?.textContent==='Copper Ore'");
            js(s,"document.querySelector('[data-save]').click()");
            js(s,"location.hash='planner'");
            waitFor(s,"document.querySelector('main h1')?.textContent==='Mein Abenteuer.'");
            assertEquals("true",js(s,"!!document.querySelector('.entity[href=\"#entry/item%3ACopper\"]')"));
            s.recreate();
            waitFor(s,"!!document.querySelector('main h1')");
            js(s,"location.hash='planner'");
            waitFor(s,"document.querySelector('main h1')?.textContent==='Mein Abenteuer.'");
            assertEquals("true",js(s,"!!document.querySelector('.entity[href=\"#entry/item%3ACopper\"]')"));
            assertEquals("true",js(s,"document.documentElement.scrollWidth<=innerWidth"));
        }
    }
    @Test public void localGraphicsAndWorldSevenLoad() throws Exception {
        try(ActivityScenario<MainActivity> s=ActivityScenario.launch(MainActivity.class)){
            waitFor(s,"!!document.querySelector('main h1')");
            js(s,"location.hash='world/7'");
            waitFor(s,"document.querySelector('main h1')?.textContent==='Shimmerfin Deep'");
            waitFor(s,"Array.from(document.querySelectorAll('main img')).filter(i=>i.getBoundingClientRect().top<innerHeight).every(i=>i.complete&&i.naturalWidth>0)");
            js(s,"location.hash='tools'");
            waitFor(s,"!!document.querySelector('#drop-form')");
            js(s,"document.querySelector('#drop-form button').click()");
            waitFor(s,"document.querySelector('#drop-result')?.textContent.includes('63,230')===true");
        }
    }
}
