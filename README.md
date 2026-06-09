# Financas por Cartao

Aplicativo local para registrar gastos de cartao em tempo quase real e usar a fatura mensal como conferencia.

## Rodar

Use o Python incluido no ambiente do Codex:

```powershell
& 'C:\Users\atomo\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe' server.py
```

Depois abra:

```text
http://127.0.0.1:8765
```

O banco SQLite fica em `data/financas.sqlite` e os PDFs importados ficam em `data/uploads/`.

## MVP implementado

- Cadastro de cartoes.
- Cadastro e arquivamento de categorias.
- Lancamento manual de gastos por cartao.
- Detalhamento de uma compra em multiplas categorias.
- Upload de fatura PDF por cartao e mes.
- Conciliacao automatica por cartao, valor, data aproximada e descricao similar.
- Criacao de gasto a partir de linha de fatura nao conciliada.
- Dashboard mensal por categoria e por cartao.
