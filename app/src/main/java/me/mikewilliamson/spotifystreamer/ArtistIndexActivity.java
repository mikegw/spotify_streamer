package me.mikewilliamson.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ArtistIndexActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_index);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ArtistIndexFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist_index, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ArtistIndexFragment extends Fragment {

        public Toast notificationToast;

        final String[] searchResults = {
                "Result 1",
                "Result 2",
                "Result 3",
                "Result 4",
                "Result 5"
        };

        final ArrayList<String>  searchResultsList = new ArrayList<String>(
                Arrays.asList(searchResults)
        );

        ArrayAdapter<String> searchResultsAdapter;

        public ArtistIndexFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_artist_index, container, false);


            searchResultsAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.artist_list_item,
                    searchResultsList

            );

            ListView searchResultsView = (ListView) rootView.findViewById(R.id.artist_index_listview);
            searchResultsView.setAdapter(searchResultsAdapter);

            EditText search = (EditText) rootView.findViewById(R.id.artist_index_search);
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String search = s.toString();
                    if(search.length() > 0) {
                        new FetchSearchResultsTask().execute(search);
                    } else {
                        searchResultsAdapter.clear();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            });

            search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        hideKeyboard();
                        return true;
                    }
                    return false;
                }
            });


            return rootView;
        }

        // HT ekjyot from http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
        public void hideKeyboard() {
            // Check if no view has focus:
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }

        public class FetchSearchResultsTask extends AsyncTask<String, Void, List<Artist>> {

            final String LOG_TAG = getClass().getSimpleName();

            protected List<Artist> doInBackground(String... params) {
                String search = params[0];
                Log.v(LOG_TAG, "Searching for: " + search);
                ArtistsPager artistsPager = searchForArtists(search);
                List<Artist> artists = artistsPager.artists.items;
                return artists;
            }

            protected void onPostExecute(List<Artist> artists) {
                searchResultsAdapter.clear();
                if (artists.size() > 0) {
                    if(notificationToast != null) notificationToast.cancel();

                    for(ArtistSimple artist: artists){
                        searchResultsAdapter.add(artist.name);
                    }
                } else {
                    Log.v(LOG_TAG, "NO ARTISTS");
                    int duration = Toast.LENGTH_SHORT;
                    notificationToast = Toast.makeText(getActivity(), R.string.no_artists_available, duration);
                    notificationToast.show();
                }
                super.onPostExecute(artists);
            }

            private ArtistsPager searchForArtists(String search) {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();

                ArtistsPager searchResults = spotify.searchArtists(search);
                return searchResults;
            }

        }
    }
}
