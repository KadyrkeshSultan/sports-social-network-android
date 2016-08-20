package vn.datsan.datsan.serverdata.chat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.datsan.datsan.models.chat.Member;
import vn.datsan.datsan.utils.Constants;

/**
 * Created by yennguyen on 8/13/16.
 */
public class MemberService {

    private DatabaseReference memberDatabaseRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_MEMBERS);
    private static MemberService instance = new MemberService();

    private MemberService() {}

    public static MemberService getInstance() {
        return instance;
    }

    public void add(String chatId, Member member) {
        getMemberDatabaseRef(chatId).setValue(member.toUserRoleMap());
    }

    public DatabaseReference getMemberDatabaseRef(String chatId) {
        return memberDatabaseRef.child(chatId);
    }

    // Get members uid for a chat
    public List<String> getMembers(String chatId) {

        final List<String> members = new ArrayList<>();
        getMemberDatabaseRef(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;

                HashMap<String, String> memberMap = (HashMap<String, String>) dataSnapshot.getValue();
                for (Map.Entry<String, String> entry : memberMap.entrySet()) {
                    members.add(entry.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return members;
    }
}
