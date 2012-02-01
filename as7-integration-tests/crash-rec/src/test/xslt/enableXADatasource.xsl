<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:d="urn:jboss:domain:datasources:1.0">

	<!-- An XSLT style sheet which will enable JTS, by adding the JTS attribute 
		to the transactions subsystem, and turning on transaction propagation in 
		the JacORB subsystem. -->
	<!-- traverse the whole tree, so that all elements and attributes are eventually 
		current node -->
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="//d:subsystem/d:datasources">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
			<xa-datasource jndi-name="java:jboss/datasources/H2XADS"
				pool-name="java:jboss/datasources/H2XADS" enabled="true" use-ccm="false">
				<xa-datasource-property name="URL">
					jdbc:h2:file:crashrecdb;DB_CLOSE_ON_EXIT=FALSE
				</xa-datasource-property>
				<driver>
					h2
				</driver>
				<xa-pool>
					<is-same-rm-override>
						false
					</is-same-rm-override>
					<interleaving>
						false
					</interleaving>
					<pad-xid>
						false
					</pad-xid>
					<wrap-xa-resource>
						false
					</wrap-xa-resource>
				</xa-pool>
				<security>
					<user-name>
						sa
					</user-name>
					<password>
						sa
					</password>
				</security>
				<recovery>
					<recover-credential>
						<user-name>
							sa
						</user-name>
						<password>
							sa
						</password>
					</recover-credential>
				</recovery>
				<validation>
					<validate-on-match>
						false
					</validate-on-match>
					<background-validation>
						false
					</background-validation>
					<background-validation-millis>
						0
					</background-validation-millis>
				</validation>
				<statement>
					<prepared-statement-cache-size>
						0
					</prepared-statement-cache-size>
					<share-prepared-statements>
						false
					</share-prepared-statements>
				</statement>
			</xa-datasource>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
