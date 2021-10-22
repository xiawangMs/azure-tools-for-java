/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package azureaddemo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringController {
    @GetMapping("group1")
    @PreAuthorize("hasRole('ROLE_group1')")
    public String group1() {
        return "Hello Group 1 Users!";
    }

    @GetMapping("group2")
    @PreAuthorize("hasRole('ROLE_group2')")
    public String group2() {
        return "Hello Group 2 Users!";
    }
}
