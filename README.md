# Emissor NF-e SEBRAE Extrator

![tela-inicial](img.png)

### Extrair NF-e e CT-e do database do Emissor SEBRAE (NF-e 4.01 e CT-3 3.00)
Software para extrair os XML que estão na pasta database do antigo sistema de emissão de NF-e e CT-e do SEBRAE (NF-e 4.01 e CT-e 3.00).
Para utilizar o NF-e Extrator, basta compilar a classe **ExtractNfeZipGUI.java** e executar o programa.
Para utilizar o CT-e Extrator, basta compilar a classe **ExtractCteZipGUI.java** e executar o programa.
É necessário o Java 17 ou superior instalado.
Na tela inicial de qualquer um dos programas (NF-e ou CT-e), clique o botão [1] e informe o caminho da pasta *database/NFE_400* ou *database/CTE_300*. Depois clique no botão [2] para extrair os arquivos.
Para extrair uma lista com informações sobre NF-e/CT-e, clique no botão [3].


### Pre-requisitos

```
-> Java 17 ou superior (testado apenas com o openjdk-17 no Linux Debian 12)
```

### Como usar
**NF-e Extrator**
_NO LINUX_
```
Via linha de comando, entre no diretório ./NFe/
Execute o script: compile_run.sh
```

_NO WINDOWS_
```
Via linha de comando, entre no diretório .\NFe\
Execute o script: compile_run.cmd
```

**CT-e Extrator**
_NO LINUX_
```
Via linha de comando, entre no diretório ./CTe/
Execute o script: compile_run.sh
```

_NO WINDOWS_
```
Via linha de comando, entre no diretório .\CTe\
Execute o script: compile_run.cmd
```

### Detalhes técnicos
O banco de dados utilizado pelo Emissor do SEBRAE é o *Apache Derby Embedded*.
A consulta ao banco de dados é realizada utilizando **jdbc + derby.jar**.

**NF-e Extrator**
Os fontes estão no diretório ./NFe/.
O nome do banco utilizado pelo Emissor do SEBRAE é **NFE**. A tabela que contém as NF-e chama-se **NOTA_FISCAL** e o cadastro dos emitentes estão na tabela **EMITENTE**.
Os XMLs que estão no banco de dados estão zipados e ao baixa-los, serão salvos no diretório ./NFE_ZIP/ (que será criado caso não exista).
Os arquivos zip são salvos, um a um, com o seguinte nome: [numeroSequencial_DOCUMENTO_EMIT_SERIE-NUMERO.zip]
No qual: *DOCUMENTO_EMIT* é o **CPF** ou **CNPJ** do emitente da NF-e, *SERIE* é o número de série da NF-e e *NUMERO* é o número da NF-e emitida.
Ex:. `1_22222222222_001-000000001.zip, 2_33333333333333_001-000000012.zip, ..., 125_22222222222_007-000011026.zip`

**CT-e Extrator**
Os fontes estão no diretório ./NFe/.
O nome do banco utilizado pelo Emissor do SEBRAE é **CTE**. A tabela que contém os CT-e chama-se **CONHECIMENTO_TRANSPORTE** e o cadastro dos emitentes estão na tabela **EMITENTE**.
Os XMLs que estão no banco de dados estão zipados e ao baixa-los, serão salvos no diretório ./CTE_ZIP/ (que será criado caso não exista).
Os arquivos zip são salvos, um a um, com o seguinte nome: [numeroSequencial_DOCUMENTO_EMIT_SERIE-NUMERO.zip]
No qual: *DOCUMENTO_EMIT* é o **CNPJ** do emitente do CT-e, *SERIE* é o número de série do CT-e e *NUMERO* é o número do CT-e emitido.
Ex:. `1_22222222222222_001-000000001.zip, 2_33333333333333_001-000000012.zip, ..., 125_22222222222222_007-000011026.zip`


---

```
@dnat
```

