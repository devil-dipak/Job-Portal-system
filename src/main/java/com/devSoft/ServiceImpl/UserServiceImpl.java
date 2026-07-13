package com.devSoft.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.devSoft.Model.User;
import com.devSoft.Repository.UserRepository;
import com.devSoft.Service.UserService;

@Service
public class UserServiceImpl  implements UserService{

	@Autowired
	private UserRepository userRepo;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Override
	public void signup(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepo.save(user);
	}

	@Override
	public User login(String mail, String psw) {
		return userRepo.findFirstByEmail(mail)
				.filter(user -> passwordEncoder.matches(psw, user.getPassword()))
				.orElse(null);
	}

	@Override
	public boolean emailExists(String email) {
		return userRepo.findFirstByEmail(email).isPresent();
	}

	@Override
	public void updateUser(User user) {
		if (user.getPassword() != null && !user.getPassword().isEmpty()
				&& !user.getPassword().startsWith("$2a$")
				&& !user.getPassword().startsWith("$2b$")
				&& !user.getPassword().startsWith("$2y$")) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		userRepo.save(user);
	}

}
