package sfwinstaladorscript.comparabase.objects;

public class Info {
	private int			COD_PRODUTO;
	private String	VERSAO;

	public Info() {
	}

	/**
	 * Retorna o C�digo do Produto;
	 * 
	 * @return int
	 */
	public int getCOD_PRODUTO() {
		return COD_PRODUTO;
	}

	/**
	 * Seta o c�digo do Produto;
	 * 
	 * @param cOD_PRODUTO
	 */
	public void setCOD_PRODUTO(int COD_PRODUTO) {
		this.COD_PRODUTO = COD_PRODUTO;
	}

	/**
	 * Retorna a vers�o da compara��o;
	 * 
	 * @return
	 */
	public String getVERSAO() {
		return VERSAO;
	}

	/**
	 * Seta a vers�o da compara��o;
	 * 
	 * @param VERSAO
	 */
	public void setVERSAO(String VERSAO) {
		this.VERSAO = VERSAO;
	}
}
