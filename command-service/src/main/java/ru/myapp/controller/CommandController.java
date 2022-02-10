package ru.myapp.controller;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.myapp.model.CommandDto;
import ru.myapp.service.CommandService;

import java.security.Principal;

@Controller
@Timed
@Slf4j
//@Endpoint(id = "command")
public class CommandController {
	@Autowired
	private CommandService commandService;

	@PostMapping("/command/set_value")
	@ResponseBody
	public ResponseEntity<Object> setValue3(@RequestBody CommandDto commandDto, @AuthenticationPrincipal Jwt jwt, Principal principal) {
		log.error("" + principal);
		log.error("" + commandDto);
		commandDto.setUsername(principal.getName());
		commandDto.setActionType("set");
		commandDto.setEmail(getEmail());
		commandService.setValue(commandDto);
		return ResponseEntity.accepted().build();
	}

	@GetMapping("/command/set_value")
	@ResponseBody
	public ResponseEntity<Object> setValue(CommandDto commandDto, @AuthenticationPrincipal Jwt jwt, Principal principal) {
		log.error("" + principal);
		log.error("" + commandDto);
		commandDto.setUsername(principal.getName());
		commandDto.setActionType("set");
		commandDto.setEmail(getEmail());
		commandService.setValue(commandDto);
		return ResponseEntity.accepted().build();
	}

	private String getEmail() {
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		if (currentUser != null) {
			Jwt token = (Jwt) currentUser.getPrincipal();
			log.debug("" + token.getClaims());
			return token.getClaims().entrySet().stream()
					.filter(a -> a.getKey().equalsIgnoreCase("email"))
					.map(a -> a.getValue().toString()).findFirst().orElse("");
		}
		return "";
	}


	@GetMapping("/command/add_value")
	@ResponseBody
	public ResponseEntity<Object> addValue(CommandDto commandDto, @AuthenticationPrincipal Jwt jwt, Principal principal) {
		commandDto.setUsername(principal.getName());
		commandDto.setActionType("add");
		commandDto.setEmail(getEmail());
		commandService.addValue(commandDto);
		return ResponseEntity.accepted().build();

	}
}
