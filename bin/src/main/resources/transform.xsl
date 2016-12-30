<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output omit-xml-declaration="yes" indent="yes" method="xml" />

	<xsl:param name="stop-words"
		select="'les','des','une','pour','est','qui','par','dans','que','qui','sur','avec','son'" />

	<xsl:variable name="regex"
		select="concat('(^|\W)(', string-join($stop-words, '|'), ')', '(\W(', string-join($stop-words, '|'), '))*($|\W)')" />


	<xsl:template match="/">
		<Root>
			<xsl:apply-templates />
		</Root>
	</xsl:template>

	<xsl:template match="text">
		<document name="{@name}">
			<xsl:for-each-group group-by="."
				select="for $w in .//@lemma return lower-case($w)">
				<xsl:sort select="count(current-group())" order="descending" />
				<xsl:if test="string-length(.)>2">
					<xsl:analyze-string select="." regex="{$regex}">
						<xsl:non-matching-substring>
							<xsl:if test="count(current-group())>3">
								<wrd cnt="{count(current-group())}" lemme="{current-grouping-key()}" />
							</xsl:if>
						</xsl:non-matching-substring>
					</xsl:analyze-string>
				</xsl:if>
			</xsl:for-each-group>
		</document>
	</xsl:template>

</xsl:stylesheet>