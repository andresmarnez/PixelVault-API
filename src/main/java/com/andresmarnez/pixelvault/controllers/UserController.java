package com.andresmarnez.pixelvault.controllers;

import com.andresmarnez.pixelvault.model.User;
import com.andresmarnez.pixelvault.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService){
		this.userService = userService;
	}

	@GetMapping("/users/{username}")
	public User findUser(@PathVariable("username") String username){
		return userService.getByUsername(username).orElse(null);
	}

	@PostMapping("/register/")
	public int register(@RequestParam("email") String email, @RequestParam("username") String username, @RequestParam("password") String password){
		return userService.createUser(email, username, password);
	}
}
