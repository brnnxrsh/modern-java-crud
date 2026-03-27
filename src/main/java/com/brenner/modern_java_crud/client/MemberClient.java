package com.brenner.modern_java_crud.client;

import com.brenner.modern_java_crud.dto.MemberExternalCreateDto;
import com.brenner.modern_java_crud.dto.MemberExternalDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member-client", url = "http://localhost:1080")
public interface MemberClient {

    @GetMapping("/members/{id}")
    MemberExternalDto findById(@PathVariable("id") Long id);

    @PostMapping("/members")
    MemberExternalDto create(@RequestBody MemberExternalCreateDto dto);

}
