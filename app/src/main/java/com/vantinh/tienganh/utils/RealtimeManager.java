package com.vantinh.tienganh.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.DocumentSnapshot;
import com.vantinh.tienganh.CourseRequest;
import com.vantinh.tienganh.Enrollment;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class RealtimeManager {
    private static RealtimeManager instance;
    private FirebaseFirestore db;
    private List<ListenerRegistration> listeners;

    private RealtimeManager() {
        db = FirebaseFirestore.getInstance();
        listeners = new ArrayList<>();
    }

    public static RealtimeManager getInstance() {
        if (instance == null) {
            instance = new RealtimeManager();
        }
        return instance;
    }

    // Interface cho real-time callbacks
    public interface OnDataChangeListener<T> {
        void onDataChanged(List<T> data);
        void onError(Exception e);
    }

    public interface OnCountChangeListener {
        void onCountChanged(int count);
        void onError(Exception e);
    }

    // Listen to course requests cho giáo viên sử dụng teacherId
    public void listenToCourseRequests(String teacherId, OnDataChangeListener<CourseRequest> listener) {
        Log.d("RealtimeManager", "Setting up listener for teacherId: '" + teacherId + "'");

        ListenerRegistration registration = db.collection("courseRequests")
                .whereEqualTo("teacherId", teacherId) // Query trực tiếp bằng teacherId
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("RealtimeManager", "Error in snapshot listener", error);
                        listener.onError(error);
                        return;
                    }

                    List<CourseRequest> requests = new ArrayList<>();
                    if (value != null) {
                        Log.d("RealtimeManager", "Received " + value.size() + " documents for teacherId: " + teacherId);

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                CourseRequest request = doc.toObject(CourseRequest.class);
                                if (request != null) {
                                    requests.add(request);
                                    Log.d("RealtimeManager", "Added request from student: " + request.getStudentName() +
                                        " for course: " + request.getCourseName() +
                                        " with status: " + request.getStatus());
                                }
                            } catch (Exception e) {
                                Log.e("RealtimeManager", "Error processing document: " + doc.getId(), e);
                            }
                        }
                    } else {
                        Log.w("RealtimeManager", "QuerySnapshot is null");
                    }

                    Log.d("RealtimeManager", "Final result: " + requests.size() + " requests for teacherId: " + teacherId);
                    listener.onDataChanged(requests);
                });

        listeners.add(registration);
        Log.d("RealtimeManager", "Listener registered successfully. Total listeners: " + listeners.size());
    }

    // Count pending requests real-time sử dụng teacherId
    public void listenToPendingRequestsCount(String teacherId, OnCountChangeListener listener) {
        Log.d("RealtimeManager", "Setting up count listener for teacherId: " + teacherId);

        ListenerRegistration registration = db.collection("courseRequests")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("RealtimeManager", "Error in count listener", error);
                        listener.onError(error);
                        return;
                    }

                    int count = 0;
                    if (value != null) {
                        count = value.size();
                        Log.d("RealtimeManager", "Found " + count + " pending requests for teacherId: " + teacherId);
                    }
                    listener.onCountChanged(count);
                });

        listeners.add(registration);
    }

    // Cleanup tất cả listeners
    public void removeAllListeners() {
        for (ListenerRegistration registration : listeners) {
            if (registration != null) {
                registration.remove();
            }
        }
        listeners.clear();
    }
}
