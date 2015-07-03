package com.boorce.buzytrim;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import eu.chainfire.libsuperuser.Shell;
import java.util.List;


public class MainActivity extends Activity {

    private String busyPath=null;

    private int rild_pid=0;

    private class Startup extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog = null;
        private Context context = null;
        private boolean suAvailable = false;

        public Startup setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        protected void onPreExecute() {
            // We're creating a progress dialog here because we want the user to wait.
            // If in your app your user can just continue on with clicking other things,
            // don't do the dialog thing.

            dialog = new ProgressDialog(context);
            dialog.setMessage(getString(R.string.checkroot_string));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Let's do some SU stuff
            suAvailable = Shell.SU.available();
            busyPath=new sysLinuxTools().getBusyPath();
            rild_pid=new sysLinuxTools().getRildPID();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            boolean hasNoSU=false;
            boolean hasNoBusybox=false;
            dialog.dismiss();
            String message=(new StringBuilder()).append(getString(R.string.rildpid)).append(rild_pid).toString();
            ((TextView) findViewById(R.id.rildpid_text)).setText(message);
            if(suAvailable) {
                ((CheckBox) findViewById(R.id.hasSUCheckBox)).setChecked(true);
                (findViewById(R.id.fsTrimSystemButton)).setEnabled(true);
                (findViewById(R.id.fsTrimAllbutton)).setEnabled(true);
            } else {
                ((CheckBox) findViewById(R.id.hasSUCheckBox)).setChecked(false);
                (findViewById(R.id.fsTrimSystemButton)).setEnabled(false);
                (findViewById(R.id.fsTrimAllbutton)).setEnabled(false);
                hasNoSU=true;
            }
            if(busyPath==null) {
                (findViewById(R.id.fsTrimSystemButton)).setEnabled(false);
                (findViewById(R.id.fsTrimAllbutton)).setEnabled(false);
                Toast.makeText(context, getString(R.string.noBusy_string), Toast.LENGTH_LONG).show();
                ((CheckBox) findViewById(R.id.hasBusybox)).setChecked(false);
                hasNoBusybox=true;
            } else {
                ((CheckBox) findViewById(R.id.hasBusybox)).setChecked(true);
            }
            if(hasNoBusybox||hasNoSU) {
                raiseMessage(hasNoSU,hasNoBusybox);
            }
        }
    }

    private class FsTrimAll extends AsyncTask<Void, String, Void> {
        private ProgressDialog dialog = null;
        private Context context = null;

        public FsTrimAll setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        protected void onPreExecute() {
            // We're creating a progress dialog here because we want the user to wait.
            // If in your app your user can just continue on with clicking other things,
            // don't do the dialog thing.

            dialog = new ProgressDialog(context);
            dialog.setMessage(getString(R.string.checkroot_string));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        private Void fsTrim(String targetPath) {
            String suCommand;
            String message;
            List<String> returnStr;

            // Let's do some SU stuff
            message=(new StringBuilder()).append(getString(R.string.doFsTrim_string)).append(targetPath).toString();
            this.publishProgress("dialog",message);
            suCommand=(new StringBuilder()).append(busyPath).append("fstrim -v ").append(targetPath).toString();
            returnStr=Shell.SU.run(suCommand);
            if(returnStr==null) {
                message=(new StringBuilder()).append(getString(R.string.fsTrimFail_string)).append(targetPath).toString();
            } else {
                message=returnStr.toString();
            }
            this.publishProgress("toast",message);
            return null;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Let's do some SU stuff

            fsTrim("/cache");
            fsTrim("/data");
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values[0].equals("toast")) {
                Toast.makeText(MainActivity.this, values[1], Toast.LENGTH_SHORT).show();
            } else {
                dialog.setMessage(values[1]);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
        }
    }

    private class FsTrimSystem extends AsyncTask<Void, String, Void> {
        private ProgressDialog dialog = null;
        private Context context = null;

        public FsTrimSystem setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        protected void onPreExecute() {
            // We're creating a progress dialog here because we want the user to wait.
            // If in your app your user can just continue on with clicking other things,
            // don't do the dialog thing.

            dialog = new ProgressDialog(context);
            dialog.setMessage(getString(R.string.checkroot_string));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Let's do some SU stuff
            List<String> returnStr;
            String message;
            String suCommand;

            message=(new StringBuilder()).append(getString(R.string.doFsTrim_string)).append("/system").toString();
            this.publishProgress("dialog",message);
            suCommand=(new StringBuilder()).append(busyPath).append("fstrim -v /system").toString();
            returnStr=Shell.SU.run(suCommand);
            if(returnStr==null) {
                message=(new StringBuilder()).append(getString(R.string.fsTrimFail_string)).append("/system").toString();
            } else {
                message=returnStr.toString();
            }
            this.publishProgress("toast", message);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values[0].equals("toast")) {
                Toast.makeText(MainActivity.this, values[1], Toast.LENGTH_SHORT).show();
            } else {
                dialog.setMessage(values[1]);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
        }
    }

    private class reRild extends AsyncTask<Void, String, Void> {
        private ProgressDialog dialog = null;
        private Context context = null;

        public reRild setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        protected void onPreExecute() {
            // We're creating a progress dialog here because we want the user to wait.
            // If in your app your user can just continue on with clicking other things,
            // don't do the dialog thing.

            dialog = new ProgressDialog(context);
            dialog.setMessage(getString(R.string.checkroot_string));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Let's do some SU stuff
            List<String> returnStr;
            String message;
            String suCommand;

            this.publishProgress("dialog",getString(R.string.doReRILD_string));
            if(rild_pid!=0) {
                suCommand = (new StringBuilder()).append("kill -HUP ").append(rild_pid).toString();
                returnStr = Shell.SU.run(suCommand);
                if (returnStr == null) {
                    message = getString(R.string.reRILDFail_string);
                } else {
                    message = (new StringBuilder()).append(getString(R.string.rildpid)).append(rild_pid).toString();
                }
            } else {
                message = getString(R.string.reRILDFail_string);
            }
            this.publishProgress("toast", message);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values[0].equals("toast")) {
                Toast.makeText(MainActivity.this, values[1], Toast.LENGTH_SHORT).show();
            } else {
                dialog.setMessage(values[1]);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            rild_pid = new sysLinuxTools().getRildPID();
            String message = (new StringBuilder()).append(getString(R.string.rildpid)).append(rild_pid).toString();
            ((TextView) findViewById(R.id.rildpid_text)).setText(message);
            dialog.dismiss();
        }
    }

    private void raiseMessage(boolean hasNoSU, boolean hasNoBuzy) {
        String message=getString(R.string.textRootBusyFail);
        if(hasNoSU) {
            message=message+getString(R.string.textRootFail);
        }
        if(hasNoBuzy) {
            message=message+getString(R.string.textBusyFail);
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialogRootBusyFail))
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        busyPath=(new sysLinuxTools().getBusyPath());

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // fstrim data and Cache button
        (findViewById(R.id.fsTrimAllbutton)).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        (new FsTrimAll()).setContext(MainActivity.this).execute();
                    }
                });

        // fstrim system button
        (findViewById(R.id.fsTrimSystemButton)).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        (new FsTrimSystem()).setContext(MainActivity.this).execute();
                    }
                });

        (findViewById(R.id.reRild)).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        (new reRild()).setContext(MainActivity.this).execute();
                    }
                });

        (new Startup()).setContext(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_re_root) {
            (new Startup()).setContext(this).execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.finishAndRemoveTask();
    }


}
