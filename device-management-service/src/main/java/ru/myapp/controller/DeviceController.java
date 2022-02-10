package ru.myapp.controller;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.myapp.entity.Device;
import ru.myapp.service.DeviceService;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@Timed
@Slf4j
//@Endpoint(id = "management")
public class DeviceController {

	@Autowired
	private DeviceService deviceService;

	@GetMapping("/management/devices")
	@ResponseBody
	public List<Device> getDevices(@AuthenticationPrincipal Jwt jwt, Principal principal) {
		return deviceService.getDevices();
	}

	@PostMapping("/management/create_device")
	@ResponseBody
	public Device createDeviceWithSensor(@AuthenticationPrincipal Jwt jwt, Principal principal,
										 @RequestParam UUID deviceId, @RequestParam UUID sensorId, @RequestParam String parameterName) {
		return deviceService.createDeviceWithSensor(deviceId, sensorId, parameterName);
	}

	@GetMapping("/management/create_device")
	@ResponseBody
	public Device createDeviceWithSensor2(@AuthenticationPrincipal Jwt jwt, Principal principal,
										 @RequestParam UUID deviceId, @RequestParam UUID sensorId, @RequestParam String parameterName) {
		log.info("Request: " + deviceId + " : " + sensorId + " : " + parameterName);
		return deviceService.createDeviceWithSensor(deviceId, sensorId, parameterName);
	}
}
