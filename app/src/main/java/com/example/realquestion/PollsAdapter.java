package com.example.realquestion;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PollsAdapter extends RecyclerView.Adapter<PollsAdapter.ViewHolder> {

    private final List<poll> pollList;

    public PollsAdapter(List<poll> polls) {
        pollList=polls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View pollView = inflater.inflate(R.layout.poll, parent, false);
        ViewHolder viewHolder = new ViewHolder(pollView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference answersRef = FirebaseDatabase.getInstance("https://sign-12108a-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("answers");
        String userId = currentUser.getUid();
        poll Poll = pollList.get(position);
        holder.textView.setText(Poll.question);
        holder.b1.setText(Poll.c1);
        holder.b2.setText(Poll.c2);
        holder.b3.setText(Poll.c3);
        holder.b4.setText(Poll.c4);
        isUserInList(answersRef, Poll.id, userId, new UserInListCallback() {
            @Override
            public void onCallback(boolean isUserInList) {
                if (isUserInList) {
                    holder.b1.setEnabled(false);
                    holder.b2.setEnabled(false);
                    holder.b3.setEnabled(false);
                    holder.b4.setEnabled(false);
                }
            }
        });

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.b1.setSelected(false);
                holder.b2.setSelected(false);
                holder.b3.setSelected(false);
                holder.b4.setSelected(false);


                isUserInList(answersRef, Poll.id, userId, new UserInListCallback() {
                    @Override
                    public void onCallback(boolean isUserInList) {
                        Log.d("GFDPOGKIPODgm", String.valueOf(isUserInList));
                        if (isUserInList) {
                        } else {
                            String key = "c" + v.getResources().getResourceEntryName(v.getId()).substring(1);
                            Log.d("MYKEY", key);
                            AddAnswer(Poll.id, key);
                        }
                    }
                });
                v.setBackgroundResource(R.color.black);
                holder.b1.setEnabled(v == holder.b1);
                holder.b2.setEnabled(v == holder.b2);
                holder.b3.setEnabled(v == holder.b3);
                holder.b4.setEnabled(v == holder.b4);
            }
        };

        holder.b1.setOnClickListener(clickListener);
        holder.b2.setOnClickListener(clickListener);
        holder.b3.setOnClickListener(clickListener);
        holder.b4.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return pollList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private Button b1, b2, b3, b4;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView= itemView.findViewById(R.id.question_text);
            b1 = itemView.findViewById(R.id.c1);
            b2 = itemView.findViewById(R.id.c2);
            b3 = itemView.findViewById(R.id.c3);
            b4 = itemView.findViewById(R.id.c4);

        }
    }
    public void isUserInList(DatabaseReference answersRef, String answerId, String userId, final UserInListCallback callback) {
        DatabaseReference answerRef = answersRef.child(answerId);
        DatabaseReference usersRef = answerRef.child("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isUserInList = false;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userValue = userSnapshot.getValue(String.class);
                    if (userId.equals(userValue)) {
                        isUserInList = true;
                        break;
                    }
                }
                onComplete(isUserInList);
            }
            public void onComplete(boolean isUserInList) {
                callback.onCallback(isUserInList);
                Log.d("TAG", "User is in list: " + isUserInList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
    public interface UserInListCallback {
        void onCallback(boolean isUserInList);
    }
    public void AddAnswer(String id, String key) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference answerRef = FirebaseDatabase.getInstance().getReference().child("answers").child(id);
        String userId = currentUser.getUid();

        answerRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Object c1Value = dataSnapshot.getValue();
                if (c1Value != null) {
                    int c1IntValue = ((Long) c1Value).intValue();
                    int newC1IntValue = c1IntValue + 1;
                    answerRef.child(key).setValue(newC1IntValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        answerRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> usersList = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    usersList.add(userId);
                }
                if (!usersList.contains(userId)) {
                    usersList.add(userId);
                }
                answerRef.child("users").setValue(usersList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибки
                // ...
            }
        });


    }
}
