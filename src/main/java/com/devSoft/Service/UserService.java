package com.devSoft.Service;

import com.devSoft.Model.User;

public interface UserService {
	
	void signup (User user);
	User login(String mail, String psw);
	boolean emailExists(String email);
	void updateUser(User user);

}
