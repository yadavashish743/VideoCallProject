package io.agora.openvcall.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.Toast;

import io.agora.propeller.UserStatusData;
import io.agora.propeller.VideoInfoData;
import io.agora.openvcall.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class VideoViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static Logger log = LoggerFactory.getLogger(VideoViewAdapter.class);

    protected final LayoutInflater mInflater;
    protected final Context mContext;

    protected final ArrayList<UserStatusData> mUsers;

    protected final VideoViewEventListener mListener;

    protected int mLocalUid;

    public VideoViewAdapter(Context context, int localUid, HashMap<Integer, SoftReference<SurfaceView>> uids, VideoViewEventListener listener) {
        mContext = context;
        mInflater = ((Activity) context).getLayoutInflater();

        mLocalUid = localUid;

        mListener = listener;

        mUsers = new ArrayList<>();

        init(uids);
    }

    protected int mItemWidth;
    protected int mItemHeight;

    private int mDefaultChildItem = 0;

    private void init(HashMap<Integer, SoftReference<SurfaceView>> uids) {
        mUsers.clear();

        customizedInit(uids, true);
    }

    protected abstract void customizedInit(HashMap<Integer, SoftReference<SurfaceView>> uids, boolean force);

    public abstract void notifyUiChanged(HashMap<Integer, SoftReference<SurfaceView>> uids, int uidExtra, HashMap<Integer, Integer> status, HashMap<Integer, Integer> volume);

    protected HashMap<Integer, VideoInfoData> mVideoInfo; // left user should removed from this HashMap

    public void addVideoInfo(int uid, VideoInfoData video) {
        if (mVideoInfo == null) {
            mVideoInfo = new HashMap<>();
        }
        mVideoInfo.put(uid, video);
    }

    public void cleanVideoInfo() {
        mVideoInfo = null;
    }

    public void setLocalUid(int uid) {
        mLocalUid = uid;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup v = (ViewGroup) mInflater.inflate(R.layout.video_view_container, parent, false);
        v.getLayoutParams().width = mItemWidth;
        v.getLayoutParams().height = mItemHeight;
        mDefaultChildItem = v.getChildCount();
        return new VideoUserStatusHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoUserStatusHolder myHolder = ((VideoUserStatusHolder) holder);
        int itemPosition = holder.getAdapterPosition();

        final UserStatusData user = mUsers.get(position);

        log.debug("onBindViewHolder " + position + " " + user + " " + myHolder + " " + myHolder.itemView + " " + mDefaultChildItem);

        FrameLayout holderView = (FrameLayout) myHolder.itemView;

        holderView.setOnTouchListener(new OnDoubleTapListener(mContext) {
            @Override
            public void onDoubleTap(View view, MotionEvent e) {
                if (mListener != null) {
                    mListener.onItemDoubleClick(view, user);
                }
            }

            @Override
            public void onSingleTapUp() {
            }

        });
        holderView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(mContext).setTitle("Remove Video call! ").setMessage("Are you sure you want to remove this call ?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(mContext,"Removing Call",Toast.LENGTH_SHORT).show();
                                mUsers.remove(itemPosition);
                                notifyItemRemoved(itemPosition);
                                notifyItemRangeChanged(itemPosition,mUsers.size());

                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).show();
                return false;
            }
        });

        if (holderView.getChildCount() == mDefaultChildItem) {
            SurfaceView target = user.mView.get();
            VideoViewAdapterUtil.stripView(target);
            holderView.addView(target, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        VideoViewAdapterUtil.renderExtraData(mContext, user, myHolder);
    }

    @Override
    public int getItemCount() {
        log.debug("getItemCount " + mUsers.size());
        return mUsers.size();
    }

    @Override
    public long getItemId(int position) {
        UserStatusData user = mUsers.get(position);

        SurfaceView view = user.mView.get();
        if (view == null) {
            throw new NullPointerException("SurfaceView destroyed for user " + user.mUid + " " + user.mStatus + " " + user.mVolume);
        }

        return (String.valueOf(user.mUid) + System.identityHashCode(view)).hashCode();
    }
}
