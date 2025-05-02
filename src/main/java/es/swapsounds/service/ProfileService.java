package es.swapsounds.service;

import es.swapsounds.model.User;
import org.springframework.stereotype.Service;

import java.sql.Blob;

@Service
public class ProfileService {

    public String getUserInitial(User user) {
        Blob profileImage = user.getProfilePicture();
        if (profileImage == null) {
            return user.getUsername().substring(0, 1).toUpperCase();
        }
        return "";
    }
}
