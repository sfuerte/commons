package com.savage7.maven.plugin.dependency;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.StringUtils;

/**
 * ArtifactItem represents information specified in the plugin configuration
 * section for each artifact.
 * 
 * @since 1.0
 * @author <a href="mailto:robert@savage7.com">Robert Savage</a>
 * @version 1.0
 */
public class ArtifactItem
{
    /**
     * Group Id of Artifact
     * 
     * @parameter
     * @required
     */
    private String groupId;

    /**
     * Name of Artifact
     * 
     * @parameter
     * @required
     */
    private String artifactId;

    /**
     * Version of Artifact
     * 
     * @parameter
     */
    private String version = null;


    /**
     * Classifier for Artifact (tests,sources,etc)
     * 
     * @parameter
     */
    private String classifier;

    /**
     * Local file to download artifact to.
     * Location file to install artifact from. 
     * 
     * @parameter default-value="{artifactId}-{version}.{packaging}"
     * 
     */
    private String localFile = "{artifactId}-{version}.{packaging}";
    
    /**
     * URL to download artifact from.
     * 
     * @parameter
     * @required
     */
    private String stagingDirectory;    

    /**
     * URL to download artifact from.
     * 
     * @parameter
     */
    private String downloadUrl;    

    /**
     * Packaging type of the artifact to be installed. 
     *
     * @parameter default-value="jar"
     * @required
     */
    private String packaging;

    /**
     * Installs the artifact into the local maven repository
     *
     * @parameter default-value="true"
     */
    private Boolean install = true;

    /**
     * Deploys the artifact to a remote maven repository
     *
     * @parameter default-value="true"
     */
    private Boolean deploy = true;

    /**
     * Forces a download, maven install, maven deploy
     *
     * @parameter default-value="false"
     */
    private Boolean force = false;
    
    /**
     * Location of an existing POM file to be installed alongside the main artifact, given by the {@link #file}
     * parameter.
     *
     * @parameter expression="${pomFile}"
     */
    private File pomFile;

    /**
     * Generate a minimal POM for the artifact if none is supplied via the parameter {@link #pomFile}. Defaults to
     * <code>true</code> if there is no existing POM in the local repository yet.
     *
     * @parameter expression="${generatePom}" default-value="true"
     */
    private Boolean generatePom = true;

    /**
     * Flag whether to create checksums (MD5, SHA-1) or not.
     *
     * @parameter expression="${createChecksum}" 
     */
    private String createChecksum;   

    
    public ArtifactItem()
    {
        // default constructor
    }

    public ArtifactItem( Artifact artifact )
    {
        this.setArtifactId( artifact.getArtifactId() );
        this.setClassifier( artifact.getClassifier() );
        this.setGroupId( artifact.getGroupId() );
        this.setPackaging( artifact.getType() );
        this.setVersion( artifact.getVersion() );
    }

    private String filterEmptyString( String in )
    {
        if ( in == null || in.equals( "" ) )
        {
            return null;
        }
        else
        {
            return in;
        }
    }

    /**
     * @return Returns the artifactId.
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * @param artifactId
     *            The artifactId to set.
     */
    public void setArtifactId( String artifact )
    {
        this.artifactId = filterEmptyString( artifact );
    }

    /**
     * @return Returns the groupId.
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * @param groupId
     *            The groupId to set.
     */
    public void setGroupId( String groupId )
    {
        this.groupId = filterEmptyString( groupId );
    }

    /**
     * @return Returns the type.
     */
    public String getType()
    {
        return getPackaging();
    }

    /**
     * @return Returns the version.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * @param version
     * The version to set.
     */
    public void setVersion( String version )
    {
        this.version = filterEmptyString( version );
    }

    /**
     * @return Classifier.
     */
    public String getClassifier()
    {
        return classifier;
    }

    /**
     * @param classifier
     * Classifier.
     */
    public void setClassifier( String classifier )
    {
        this.classifier = filterEmptyString( classifier );
    }

    public String toString()
    {
        if ( this.classifier == null )
        {
            return groupId + ":" + artifactId + ":" + StringUtils.defaultString( version, "?" ) + ":" + packaging;
        }
        else
        {
            return groupId + ":" + artifactId + ":" + classifier + ":" + StringUtils.defaultString( version, "?" )
                + ":" + packaging;
        }
    }

    /**
     * @return Returns the location.
     */
    public String getLocalFile()
    {
        return replaceTokens(localFile);
    }

    /**
     * @param sourceFileName
     * The localFile to set.
     */
    public void setLocalFile( String localFile )
    {
        this.localFile = filterEmptyString( localFile );
    }

    /**
     * @return Returns the stagingDirectory.
     */
    public String getStagingDirectory()
    {
        return replaceTokens(stagingDirectory);
    }

    /**
     * @param stagingDirectory
     * The stagingDirectory to set.
     */
    public void setStagingDirectory( String stagingDirectory )
    {
        this.stagingDirectory = filterEmptyString( stagingDirectory );
    }
    
    /**
     * @return Returns the source URL to download the artifact.
     */
    public String getDownloadUrl()
    {
        return replaceTokens(downloadUrl);
    }

    /**
     * @param sourceFileName
     * Set the URL to download the artifact from.
     */
    public void setDownloadUrl( String downloadUrl )
    {
        this.downloadUrl = filterEmptyString( downloadUrl );
    }
    

    /**
     * @return Packaging.
     */
    public String getPackaging()
    {
        return packaging;
    }

    /**
     * @param packaging
     * Packaging.
     */
    public void setPackaging( String packaging )
    {
        this.packaging = filterEmptyString( packaging );
    }
    
    
    /**
     * @return Force.
     */
    public Boolean getForce()
    {
        return force;
    }

    /**
     * @param force
     * Force.
     */
    public void setForce( Boolean force )
    {
        this.force = force;
    }

    /**
     * @return Install.
     */
    public Boolean getInstall()
    {
        return install;
    }

    /**
     * @param install
     * Install.
     */
    public void setInstall( Boolean install )
    {
        this.install = install;
    }

    /**
     * @return Deploy.
     */
    public Boolean getDeploy()
    {
        return deploy;
    }

    /**
     * @param deploy
     * Deploy.
     */
    public void setDeploy( Boolean deploy )
    {
        this.deploy = deploy;
    }

    
    /**
     * @return PomFile.
     */
    public File getPomFile()
    {
        return pomFile;
    }

    /**
     * @param pomFile
     * PomFile.
     */
    public void setPomFile( File pomFile )
    {
        this.pomFile = pomFile;
    }

    /**
     * @return GeneratePom.
     */
    public Boolean getGeneratePom()
    {
        return generatePom;
    }

    /**
     * @param generatePom
     * GeneratePom.
     */
    public void setGeneratePom( Boolean generatePom )
    {
        this.generatePom = generatePom;
    }
    
    
    /**
     * @return CreateChecksum.
     */
    public String getCreateChecksum()
    {
        return createChecksum;
    }

    /**
     * @param createChecksum
     * CreateChecksum.
     */
    public void setCreateChecksum( String createChecksum )
    {
        this.createChecksum = createChecksum;
    }    
    
    private String replaceTokens(String target)
    {
        if(target == null)
               return null;

        if(target.isEmpty())
            return target;
        
        // replace all tokens
        if(getGroupId() != null)
            target = target.replace("{groupId}", getGroupId());
        
        if(getArtifactId() != null)
            target = target.replace("{artifactId}", getArtifactId());
        
        if(getVersion() != null)
            target = target.replace("{version}", getVersion());
        
        if(getPackaging() != null)
            target = target.replace("{packaging}", getPackaging());
        
        if(getClassifier() != null)
            target = target.replace("{classifier}", getClassifier());
        
        if(getType() != null)
            target = target.replace("{type}", getType());
        
        return target;
    }
    
}