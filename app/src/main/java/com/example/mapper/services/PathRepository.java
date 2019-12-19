package com.example.mapper.services;

// Imports
import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.mapper.services.database.ApplicationDatabase;
import com.example.mapper.services.models.Path;
import com.example.mapper.services.models.PathDAO;
import com.example.mapper.services.models.Point;
import com.example.mapper.services.models.RepoInsertCallback;
import java.util.List;

/**
 * @author Ellis Squires
 * @version 1.0
 * @since 1.0
 */
public class PathRepository extends ViewModel {

    private final PathDAO pathDAO;

    public PathRepository(Application application) {
        ApplicationDatabase db = ApplicationDatabase.getDatabase(application);
        pathDAO = db.pathDao();
    }

    /**
     * Create a new path object and add it to the database..
     * @param riCB a RepoInsertCallback which will be called when the path is inserted.
     */
    public void createPath(RepoInsertCallback riCB){
        new InsertPathAsyncTask(pathDAO, riCB).execute(new Path());
    }

    /**
     * Fetches all the points on a path
     * @param pathId
     * @return LiveData<List<Point>>
     */
    public LiveData<List<Point>> getPointsOnPath(long pathId) {
        return pathDAO.findPointsOnPath(pathId);
    }

    /**
     * Handles inserting paths on a background thread
     */
    static class InsertPathAsyncTask extends AsyncTask<Path, Void, Long> {
        private PathDAO asyncPathDao;
        private RepoInsertCallback mCallback;

        private InsertPathAsyncTask(PathDAO dao, RepoInsertCallback riCB){
            asyncPathDao = dao;
            mCallback = riCB;
        }

        @Override
        protected Long doInBackground(final Path... params){
            return asyncPathDao.insert(params[0]);
        }

        protected void onPostExecute(Long result) {
            mCallback.OnFinishInsert(result);
        }
    }

}
