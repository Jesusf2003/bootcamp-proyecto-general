package com.banco.yanki.controller;

import com.banco.yanki.dto.SendMoneyRequest;
import com.banco.yanki.dto.WalletRequest;
import com.banco.yanki.dto.WalletResponse;
import com.banco.yanki.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/** Monedero movil Yanki: registro por numero de celular y envio de dinero entre celulares. */
@Slf4j
@RestController
@RequestMapping("/api/v1/wallets")
@Tag(name = "Yanki", description = "Monedero movil: envio de dinero usando solo el numero de celular")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(summary = "Registrar un monedero Yanki vinculado a una cuenta de debito")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<WalletResponse> register(@Valid @RequestBody WalletRequest request) {
        log.info("POST /wallets telefono={}", request.getPhoneNumber());
        return walletService.register(request);
    }

    @Operation(summary = "Consultar el monedero asociado a un numero de celular")
    @GetMapping("/{phoneNumber}")
    public Mono<WalletResponse> findByPhoneNumber(@PathVariable String phoneNumber) {
        return walletService.findByPhoneNumber(phoneNumber);
    }

    @Operation(summary = "Enviar dinero de un celular a otro")
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> sendMoney(@Valid @RequestBody SendMoneyRequest request) {
        log.info("POST /wallets/send de={} a={} monto={}",
                request.getFromPhoneNumber(), request.getToPhoneNumber(), request.getAmount());
        return walletService.sendMoney(request);
    }
}
