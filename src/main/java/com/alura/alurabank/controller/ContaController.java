package com.alura.alurabank.controller;

import com.alura.alurabank.controller.form.ContaCorrenteForm;
import com.alura.alurabank.controller.form.CorrentistaForm;
import com.alura.alurabank.dominio.ContaCorrente;
import com.alura.alurabank.dominio.Correntista;
import com.alura.alurabank.dominio.MovimentacaoDeConta;
import com.alura.alurabank.repositorio.RepositorioContasCorrente;
import com.googlecode.jmapper.JMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/contas")
public class ContaController {

    @Autowired
    private RepositorioContasCorrente repositorioContasCorrente;

    @Autowired
    private JMapper<ContaCorrente, ContaCorrenteForm> contaCorrenteMapper;

    @GetMapping
    public String consultarSaldo(
            @RequestParam(name = "banco") String banco,
            @RequestParam(name = "agencia") String agencia,
            @RequestParam(name = "numero") String numero
    ){
        ContaCorrente contaCorrente = repositorioContasCorrente.buscar(banco, agencia, numero).orElse(new ContaCorrente());

        return String.format("Banco: %s, Agencia: %s, Conta: %s, Saldo: %s", banco, agencia, numero,contaCorrente.lerSaldo());
    }

    @PostMapping
    public ResponseEntity<ContaCorrente> criarNovaConta(@RequestBody CorrentistaForm correntistaForm) {
        Correntista correntista = correntistaForm.toCorrentista();
        String banco = "333";
        String agencia = "2222";
        String numero = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        ContaCorrente conta = new ContaCorrente(banco, agencia, numero, correntista);
        repositorioContasCorrente.salvar(conta);
        return ResponseEntity.status(HttpStatus.CREATED).body(conta);
    }

    @PutMapping
    public ResponseEntity<String> movimentarConta(@RequestBody MovimentacaoDeConta movimentacao) {
        Optional<ContaCorrente> opContaCorrente =
                repositorioContasCorrente.buscar(movimentacao.getBanco(),
                movimentacao.getAgencia(),
                movimentacao.getNumero());

        if(opContaCorrente.isEmpty()) {
            return ResponseEntity.badRequest().body("Conta corrente não existe");
        } else {
            ContaCorrente contaCorrente = opContaCorrente.get();
            movimentacao.executarEm(contaCorrente);
            repositorioContasCorrente.salvar(contaCorrente);
            return ResponseEntity.ok("Movimentação realizada com sucesso");
        }
    }
    
    @DeleteMapping
    public String fecharConta(@RequestBody ContaCorrenteForm contaForm) {
        ContaCorrente conta = contaCorrenteMapper.getDestination(contaForm);
        repositorioContasCorrente.fechar(conta);
        return "Conta fechada com sucesso";
    }
}
