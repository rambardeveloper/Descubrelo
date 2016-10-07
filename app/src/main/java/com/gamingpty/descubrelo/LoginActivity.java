package com.gamingpty.descubrelo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.services.AccountService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";

    // App
    private ProgressDialog mProgressDialog;
    private PrefManager pref;
    Button loginRegButton;

    // Google
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private static final int RC_FB_SIGN_IN = 64206;
    private static final int RC_TWITTER_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    ImageButton GoogleLogin;

    // Facebook
    CallbackManager mFacebookCallbackManager;
    LoginManager mLoginManager;
    ImageButton FacebookLogin;
    AccessTokenTracker mAccessTokenTracker;

    // Twitter
    ImageButton TwitterLogin;
    private TwitterAuthClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = new PrefManager(getApplicationContext());

        getSupportActionBar().hide();

        loginRegButton = (Button) findViewById(R.id.login_register_btn);
        loginRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, LoginRegisterActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        FacebookLogin = (ImageButton) findViewById(R.id.facebook_login);
        setupFacebookStuff();
        FacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFacebookLogin();
            }
        });

        GoogleLogin = (ImageButton) findViewById(R.id.google_login);
        setupGoogleStuff();
        GoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        TwitterLogin = (ImageButton) findViewById(R.id.twitter_login);
        /*setupTwitterStuff();
        TwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginToTwitter();
            }
        });*/


    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {

            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);

        } else {

            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            //showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }


    private void loginToTwitter() {
        client = new TwitterAuthClient();
        client.authorize(LoginActivity.this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {

                final Long twitterID = twitterSessionResult.data.getUserId();
                final String name = twitterSessionResult.data.getUserName();

                Log.d("NAMEEEEE: ", name);

                client.requestEmail(twitterSessionResult.data, new Callback<String>() {
                    @Override
                    public void success(Result<String> result) {
                        Log.d("EMAILLLL: ", result.data);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d("EMAILLLL: ", exception.getMessage());
                    }
                });
//                String url = AppConfig.OMNIAUTH;
//                Map<String, String> jsonParams = new HashMap<>();
//                jsonParams.put("email", email);
//                jsonParams.put("uid", twitterID);
//                jsonParams.put("name", name);
//                jsonParams.put("provider", "twitter");
//                Map hParams = new HashMap<>();
//                hParams.put("omniauth_callback", new JSONObject(jsonParams));
//
//                JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                        (Request.Method.POST, url, new JSONObject(hParams), new Response.Listener<JSONObject>() {
//
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                try {
//
//                                    Log.d(TAG, response.getString("info"));
//                                    if (response.getString("info").equals("Need to Register")) {
//
//                                        // Staring RegisterActivity
//                                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
//                                        intent.putExtra("email",email);
//                                        startActivityForResult(intent, 0);
//
//                                    } else {
//
//                                        pref.createLoginSession(response.getJSONObject("user").getString("email"));
//                                        User user = new User(response.getJSONObject("user").getString("id"), response.getString("auth_token"), googleID, avatar, response.getJSONObject("profile").getString("name"), response.getJSONObject("user").getString("email"));
//                                        AppController.getInstance().getPrefManager().storeUser(user);
//
//                                        // Staring MainActivity
//                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                        startActivityForResult(intent, 0);
//
//                                    }
//
//                                } catch (JSONException e) {
//
//                                    e.printStackTrace();
//                                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//
//                                }
//
//                            }
//                        }, new Response.ErrorListener() {
//
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                //showProgress(false);
//                                //mPasswordView.setError(getString(R.string.error_incorrect_password));
//                                //mPasswordView.requestFocus();
//                                // TODO Auto-generated method stub
//
//                            }
//                        }) {
//                    @Override
//                    public Map<String, String> getHeaders() {
//                        HashMap<String, String> headers = new HashMap<>();
//                        headers.put("Accept", "application/json");
//                        headers.put("Content-Type", "application/json");
//                        return headers;
//                    }
//                };
//                AppController.getInstance().addToRequestQueue(jsObjRequest);
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(LoginActivity.this, "failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFacebookStuff() {

        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };

        mLoginManager = LoginManager.getInstance();
        mFacebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                // App code
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {

                                Log.e("response: ", response + "");
                                try {

                                    final String facebookID = object.getString("id");
                                    final String email = object.getString("email");
                                    final String name = object.getString("name");

                                    String url = AppConfig.OMNIAUTH;
                                    Map<String, String> jsonParams = new HashMap<>();
                                    jsonParams.put("email", email);
                                    jsonParams.put("uid", facebookID);
                                    jsonParams.put("name", name);
                                    jsonParams.put("provider", "facebook");
                                    Map hParams = new HashMap<>();
                                    hParams.put("omniauth_callback", new JSONObject(jsonParams));

                                    JsonObjectRequest jsObjRequest = new JsonObjectRequest
                                            (Request.Method.POST, url, new JSONObject(hParams), new Response.Listener<JSONObject>() {

                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    try {

                                                        Log.d(TAG, response.getString("info"));
                                                        if (response.getString("info").equals("Need to Register")) {

                                                            // Staring RegisterActivity
                                                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                                            intent.putExtra("email",email);
                                                            startActivityForResult(intent, 0);

                                                        } else {

                                                            pref.createLoginSession(response.getJSONObject("user").getString("email"));
                                                            User user = new User(response.getJSONObject("user").getString("id"), response.getString("auth_token"), facebookID, "https://graph.facebook.com/"+response.getJSONObject("authorization").getString("uid")+"/picture?type=large", response.getJSONObject("profile").getString("name"), response.getJSONObject("user").getString("email"));
                                                            AppController.getInstance().getPrefManager().storeUser(user);

                                                            // Staring MainActivity
                                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                            startActivityForResult(intent, 0);

                                                        }

                                                    } catch (JSONException e) {

                                                        e.printStackTrace();
                                                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();

                                                    }

                                                }
                                            }, new Response.ErrorListener() {

                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    showProgressDialog();
                                                    //mPasswordView.setError(getString(R.string.error_incorrect_password));
                                                    //mPasswordView.requestFocus();
                                                    // TODO Auto-generated method stub

                                                }
                                            }) {
                                        @Override
                                        public Map<String, String> getHeaders() {
                                            HashMap<String, String> headers = new HashMap<>();
                                            headers.put("Accept", "application/json");
                                            headers.put("Content-Type", "application/json");
                                            return headers;
                                        }
                                    };
                                    AppController.getInstance().addToRequestQueue(jsObjRequest);

                                }catch (Exception e){
                                    e.printStackTrace();
                                }


                            }

                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email, gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }


    private void handleFacebookLogin() {
        if (AccessToken.getCurrentAccessToken() != null){
            mLoginManager.logOut();
        }else{
            mAccessTokenTracker.startTracking();
            mLoginManager.logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);

        } else if(requestCode == TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE) {

            Log.d(TAG, "Twitter reuqst code:" + requestCode);
            client.onActivityResult(requestCode, resultCode, data);

        } else {

            Log.d(TAG, "Facebook reuqst code:" + requestCode);
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupGoogleStuff() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void setupTwitterStuff() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(AppConfig.TWITTER_KEY, AppConfig.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            final String googleID = acct.getId();
            final String email = acct.getEmail();
            final String name = acct.getDisplayName();
            final String avatar;

            if (acct.getPhotoUrl() != null) {
                avatar = acct.getPhotoUrl().toString();
            } else {
                avatar = "http://s3-us-west-2.amazonaws.com/descubre-app/missing.png";
            }

            String url = AppConfig.OMNIAUTH;
            Map<String, String> jsonParams = new HashMap<>();
            jsonParams.put("email", email);
            jsonParams.put("uid", googleID);
            jsonParams.put("name", name);
            jsonParams.put("provider", "google");
            Map hParams = new HashMap<>();
            hParams.put("omniauth_callback", new JSONObject(jsonParams));

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, new JSONObject(hParams), new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {

                                Log.d(TAG, response.getString("info"));
                                if (response.getString("info").equals("Need to Register")) {

                                    // Staring RegisterActivity
                                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                    intent.putExtra("email",email);
                                    startActivityForResult(intent, 0);

                                } else {

                                    pref.createLoginSession(response.getJSONObject("user").getString("email"));
                                    User user = new User(response.getJSONObject("user").getString("id"), response.getString("auth_token"), googleID, avatar, response.getJSONObject("profile").getString("name"), response.getJSONObject("user").getString("email"));
                                    AppController.getInstance().getPrefManager().storeUser(user);

                                    // Staring MainActivity
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivityForResult(intent, 0);

                                }

                            } catch (JSONException e) {

                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();

                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //showProgress(false);
                            //mPasswordView.setError(getString(R.string.error_incorrect_password));
                            //mPasswordView.requestFocus();
                            // TODO Auto-generated method stub

                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Accept", "application/json");
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            AppController.getInstance().addToRequestQueue(jsObjRequest);

        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            //mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
