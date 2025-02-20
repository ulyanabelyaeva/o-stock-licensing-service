package com.optimagrowth.license.controller;

import com.optimagrowth.license.model.License;
import com.optimagrowth.license.service.LicenseService;
import com.optimagrowth.license.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "v1/organization/{organizationId}/license")
public class LicenseController {

    private static final Logger logger = LoggerFactory.getLogger(LicenseController.class);

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping(value = "/{licenseId}")
    public ResponseEntity<License> getLicense(@PathVariable("organizationId") String organizationId,
                                              @PathVariable("licenseId") String licenseId) {
        logger.debug("LicenseServiceController Correlation id: {}", UserContextHolder.getContext().getCorrelationId());
        License license = licenseService.getLicense(licenseId, organizationId);
        license.add(
                linkTo(methodOn(LicenseController.class)
                        .getLicense(organizationId, license.getLicenseId()))
                        .withSelfRel(),
                linkTo(methodOn(LicenseController.class)
                        .createLicense(license))
                        .withRel("createLicense"),
                linkTo(methodOn(LicenseController.class)
                        .updateLicense(license))
                        .withRel("updateLicense"),
                linkTo(methodOn(LicenseController.class)
                        .deleteLicense(license.getLicenseId()))
                        .withRel("deleteLicense")
        );
        return ResponseEntity.ok(license);
    }

    @PutMapping
    public ResponseEntity<License> updateLicense(@RequestBody License request) {
        return ResponseEntity.ok(
                licenseService.updateLicense(request)
        );
    }

    @PostMapping
    public ResponseEntity<String> createLicense(@RequestBody License request) {
        return ResponseEntity.ok(licenseService.createLicense(request).getLicenseId());
    }

    @DeleteMapping(value = "/{licenseId}")
    public ResponseEntity<String> deleteLicense(@PathVariable("licenseId") String licenseId) {
        return ResponseEntity.ok(licenseService.deleteLicense(licenseId));
    }

    /**
     * clientType - тип клиента для меж-сервисных вызовов
     * - discovery требует использовать для вызова службы организаций клиента Discovery Client и стандартный класс Spring RestTemplate;
     * - rest требует использовать для вызова службы Load Balancer расширенный шаблон RestTemplate;
     * - feign требует использовать для вызова службы через Load Balancer клиентскую библиотеку Netflix Feign.
     */
    @GetMapping(value = "/{licenseId}/{clientType}")
    public License getLicensesWithClient(@PathVariable("organizationId") String organizationId,
                                         @PathVariable("licenseId") String licenseId,
                                         @PathVariable("clientType") String clientType) {
        return licenseService.getLicense(organizationId, licenseId, clientType);
    }

}
